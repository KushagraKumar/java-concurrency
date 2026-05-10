package dev.roadmap.phase2.problems;
import java.util.concurrent.ConcurrentHashMap; import java.util.concurrent.atomic.LongAdder;
/**
 * PROBLEM 4: Thread-Safe Multiset
 * ✅ Acceptance: Correct under 50 threads. LongAdder shows 3–10x throughput vs synchronized. Document why.
 * 🎯 Staff+ Lens: When LongAdder is inappropriate. Memory trade-offs. Metrics exposure.
 */
public interface P4_ThreadSafeMultiset {
    final ConcurrentHashMap<String, LongAdder> counts = new ConcurrentHashMap<>();
    public void add(String item);
//    { /* TODO: computeIfAbsent + LongAdder */ }
    public long count(String item);
//    { /* TODO: return current count */ }
}
