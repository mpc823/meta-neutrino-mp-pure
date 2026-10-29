#include "configfile.h"

#include <cstdio>
#include <cstdlib>
#include <cstdint>
#include <cstring>
#include <string>
#include <unistd.h>

extern "C" int64_t time_monotonic_ms(void);

int main()
{
	char path[] = "/tmp/neutrino-native-config.XXXXXX";
	int fd = mkstemp(path);
	if (fd < 0)
		return 1;
	close(fd);

	CConfigFile config('=', true);
	config.setString("name", "native-x86");
	config.setInt32("answer", 42);
	config.setBool("enabled", true);
	if (!config.saveConfig(path) || !config.loadConfig(path) ||
		config.getString("name") != "native-x86" ||
		config.getInt32("answer") != 42 || !config.getBool("enabled")) {
		std::fprintf(stderr, "native CConfigFile round-trip failed\n");
		std::remove(path);
		return 1;
	}
	std::remove(path);

	const int64_t before = time_monotonic_ms();
	const int64_t after = time_monotonic_ms();
	if (before < 0 || after < before) {
		std::fprintf(stderr, "native monotonic clock test failed\n");
		return 1;
	}
	std::puts("PASS: native Neutrino config and monotonic-time core tests");
	return 0;
}
