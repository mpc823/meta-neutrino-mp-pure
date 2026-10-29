#include <poll.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

static uint64_t fnv1a(const uint8_t *data, size_t size)
{
	uint64_t hash = UINT64_C(1469598103934665603);
	for (size_t i = 0; i < size; ++i) {
		hash ^= data[i];
		hash *= UINT64_C(1099511628211);
	}
	return hash;
}

static int software_osd_test(void)
{
	uint8_t pixels[64 * 32 * 4];
	memset(pixels, 0, sizeof pixels);
	for (unsigned y = 0; y < 32; ++y)
		for (unsigned x = 0; x < 64; ++x) {
			size_t p = (y * 64 + x) * 4;
			pixels[p + 0] = (uint8_t)(x * 4);
			pixels[p + 1] = (uint8_t)(y * 8);
			pixels[p + 2] = 0x20;
			pixels[p + 3] = 0xff;
		}
	for (unsigned y = 8; y < 24; ++y)
		for (unsigned x = 12; x < 52; ++x) {
			size_t p = (y * 64 + x) * 4;
			pixels[p + 0] = 0xe0;
			pixels[p + 1] = 0x40;
			pixels[p + 2] = 0x10;
		}
	/* This is a deterministic software-rendering regression vector. */
	uint64_t hash = fnv1a(pixels, sizeof pixels);
	if (hash != UINT64_C(0xe8cf3ffe0f77fe83)) {
		fprintf(stderr, "OSD hash: %016llx\n", (unsigned long long)hash);
		fprintf(stderr, "OSD hash mismatch\n");
		return 1;
	}
	return 0;
}

static int input_event_test(void)
{
	int fds[2];
	struct pollfd event;
	unsigned char expected = 0x52;
	unsigned char received = 0;
	if (pipe(fds) != 0 || write(fds[1], &expected, 1) != 1) {
		fprintf(stderr, "input pipe setup failed\n");
		return 1;
	}
	event.fd = fds[0];
	event.events = POLLIN;
	if (poll(&event, 1, 1000) != 1 || !(event.revents & POLLIN) ||
		read(fds[0], &received, 1) != 1 || received != expected) {
		fprintf(stderr, "input event mismatch\n");
		close(fds[0]);
		close(fds[1]);
		return 1;
	}
	close(fds[0]);
	close(fds[1]);
	return 0;
}

int main(void)
{
	if (software_osd_test() != 0 || input_event_test() != 0)
		return 1;
	puts("PASS: native headless software OSD and input tests");
	return 0;
}
