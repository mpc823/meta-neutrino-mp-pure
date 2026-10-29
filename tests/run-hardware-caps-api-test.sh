#!/bin/sh
set -eu

header_dir=${1:?usage: $0 path/to/libstb-hal/include}
tmp_dir=$(mktemp -d "${TMPDIR:-/tmp}/neutrino-hw-caps.XXXXXX")
trap 'rm -rf "$tmp_dir"' EXIT HUP INT TERM

cc -std=c11 -Wall -Wextra -Werror -I"$header_dir" \
	"$(dirname "$0")/hardware-caps-api-test.c" -o "$tmp_dir/test"
"$tmp_dir/test"
echo "PASS: hardware capability API"
