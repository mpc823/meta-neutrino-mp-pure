#include "configfile.h"
#include "hardware_caps.h"

#include <cerrno>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <fcntl.h>
#include <poll.h>
#include <signal.h>
#include <string>
#include <sys/timerfd.h>
#include <unistd.h>

extern "C" int64_t time_monotonic_ms(void);

static volatile sig_atomic_t stop_requested = 0;

static void request_stop(int)
{
	stop_requested = 1;
}

static bool configure(const char *path)
{
	CConfigFile config('=', true);
	config.setString("headless_backend", "generic-pc");
	config.setBool("video_enabled", false);
	config.setBool("audio_enabled", false);
	config.setInt32("message_poll_timeout_ms", 50);
	return config.saveConfig(path) && config.loadConfig(path) &&
		config.getString("headless_backend") == "generic-pc" &&
		!config.getBool("video_enabled") && !config.getBool("audio_enabled");
}

static bool input_message_test(int fd)
{
	const unsigned char message = 0x42;
	if (write(fd, &message, sizeof(message)) != 1)
		return false;
	return true;
}

int main(int argc, char **argv)
{
	int run_ms = 50;
	if (argc == 3 && std::strcmp(argv[1], "--run-ms") == 0) {
		run_ms = std::atoi(argv[2]);
		if (run_ms < 0 || run_ms > 60000) {
			std::fprintf(stderr, "invalid --run-ms value\n");
			return 2;
		}
	} else if (argc != 1) {
		std::fprintf(stderr, "usage: %s [--run-ms milliseconds]\n", argv[0]);
		return 2;
	}

	struct sigaction action{};
	action.sa_handler = request_stop;
	sigemptyset(&action.sa_mask);
	if (sigaction(SIGINT, &action, nullptr) != 0 ||
		sigaction(SIGTERM, &action, nullptr) != 0) {
		std::perror("sigaction");
		return 1;
	}

	char config_path[] = "/tmp/neutrino-headless-config.XXXXXX";
	const int config_fd = mkstemp(config_path);
	if (config_fd < 0) {
		std::perror("mkstemp");
		return 1;
	}
	close(config_fd);
	const bool config_ok = configure(config_path);
	unlink(config_path);
	if (!config_ok) {
		std::fprintf(stderr, "headless configuration round-trip failed\n");
		return 1;
	}

	const hw_caps_t *caps = get_hwcaps();
	if (!caps || caps->video_capabilities != 0 ||
		!(caps->display_capabilities & HW_DISPLAY_CAP_FRAMEBUFFER)) {
		std::fprintf(stderr, "unexpected Generic-PC headless capabilities\n");
		return 1;
	}

	int input_pipe[2];
	if (pipe(input_pipe) != 0) {
		std::perror("pipe");
		return 1;
	}
	const int timer = timerfd_create(CLOCK_MONOTONIC, TFD_CLOEXEC);
	if (timer < 0) {
		std::perror("timerfd_create");
		close(input_pipe[0]);
		close(input_pipe[1]);
		return 1;
	}
	struct itimerspec timeout{};
	timeout.it_value.tv_sec = run_ms / 1000;
	timeout.it_value.tv_nsec = (run_ms % 1000) * 1000000L;
	if (timerfd_settime(timer, 0, &timeout, nullptr) != 0 ||
		!input_message_test(input_pipe[1])) {
		std::perror("headless event setup");
		close(timer);
		close(input_pipe[0]);
		close(input_pipe[1]);
		return 1;
	}

	bool input_seen = false;
	bool timeout_seen = false;
	struct pollfd events[2] = {{input_pipe[0], POLLIN, 0}, {timer, POLLIN, 0}};
	while (!stop_requested && !timeout_seen) {
		const int result = poll(events, 2, 1000);
		if (result < 0) {
			if (errno == EINTR)
				continue;
			std::perror("poll");
			break;
		}
		if (events[0].revents & POLLIN) {
			unsigned char received = 0;
			if (read(input_pipe[0], &received, sizeof(received)) == 1 && received == 0x42)
				input_seen = true;
		}
		if (events[1].revents & POLLIN) {
			uint64_t expirations = 0;
			if (read(timer, &expirations, sizeof(expirations)) == sizeof(expirations))
				timeout_seen = expirations > 0;
		}
	}
	close(timer);
	close(input_pipe[0]);
	close(input_pipe[1]);

	if (!input_seen || (!timeout_seen && !stop_requested) || time_monotonic_ms() < 0) {
		std::fprintf(stderr, "headless event loop did not shut down cleanly\n");
		return 1;
	}
	std::printf("PASS: native-neutrino-headless backend=%s input=%s shutdown=%s\n",
		caps->boxname, input_seen ? "ok" : "missing",
		(timeout_seen || stop_requested) ? "ok" : "missing");
	return 0;
}
