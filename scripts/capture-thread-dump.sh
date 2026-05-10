#!/usr/bin/env bash
PID=$1; OUT=${2:-thread-dump-$(date +%s).txt}
[ -z "$PID" ] && echo "Usage: $0 <pid> [output]" && exit 1
echo "📸 Capturing thread dump for PID $PID → $OUT"
jcmd $PID Thread.print -l > "$OUT"
echo "✅ Done. Analyze with: java dev.roadmap.utils.ThreadDumpAnalyzer $OUT"
