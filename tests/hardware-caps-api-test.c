#include <stdint.h>
#include <hardware_caps.h>

int main(void)
{
	hw_caps_t caps = { 0 };

	caps.video_capabilities = HW_VIDEO_CAP_HW_ACCEL |
		HW_VIDEO_CAP_H264 | HW_VIDEO_CAP_OPENMAX;
	caps.display_capabilities = HW_DISPLAY_CAP_FRAMEBUFFER |
		HW_DISPLAY_CAP_OPENGL;

	if (!(caps.video_capabilities & HW_VIDEO_CAP_H264))
		return 1;
	if (caps.video_capabilities & HW_VIDEO_CAP_MPEG2)
		return 2;
	if (!(caps.display_capabilities & HW_DISPLAY_CAP_FRAMEBUFFER))
		return 3;
	if (caps.display_capabilities & HW_DISPLAY_CAP_DRM_KMS)
		return 4;
	return 0;
}
