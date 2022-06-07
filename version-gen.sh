#!/bin/sh

DEFAULT_VERSION="5.12.0.git"

if [ -f contrib/redhat/collectd.spec ]; then
	VERSION="`grep ^Version contrib/redhat/collectd.spec | \
	awk '{print $2}'`"
fi

if test -z "$VERSION"; then
	VERSION="$DEFAULT_VERSION"
fi

printf "%s" "$VERSION"
