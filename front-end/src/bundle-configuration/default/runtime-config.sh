#!/usr/bin/env bash

# This shell script is part of the bundle configuration of the `front-end` bundle.
# When loading the `front-end` bundle with a bundle configuration on to ConductR then
# this shell script is executed at the time the `front-end` bundle gets started.
# In this simple script we set the `APPLICATION_SECRET` environment variable
# to override the `play.crypto.secret` key of the `conf/application.conf`
export APPLICATION_SECRET=PPjOW0n2aV?s@6RdiNV@7/5xJhiiKTzk[VdHjkU9YHit8sLHoJ1rp0DCCn6b=lXt
