# curl checks this with _FILE_OFFSET_BITS=64.
PR .= ".100"
do_configure:prepend() {
	export ac_cv_sizeof_off_t=8
	sed -i s:OPT_GNUTLS/bin:OPT_GNUTLS:g ${S}/configure.ac
}
