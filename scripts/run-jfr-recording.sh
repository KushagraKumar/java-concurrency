#!/usr/bin/env bash
PID=$1; DURATION=${2:-30}; FILE="concurrency-profile-$(date +%s).jfr"
echo "🔥 Starting JFR recording for ${DURATION}s on PID $PID"
jcmd $PID JFR.start name=concurrency settings=profile duration=${DURATION}s filename=$FILE
echo "✅ Recording started. Open with: jfr print $FILE or JDK Mission Control"
