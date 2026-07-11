##  ISA
An abstraction over a CPU that describes how a computer should work

### Major types of ISA:
- RISC (Reduced Instruction Set Computer): Small instruction set that's highly optimized
- CISC (Complex Instruction Set Computer): Large instruction set which contain many specialized instructions (though most are rarely practical)

## Pipelines
Pipelining a technique in which CPUs start fetching and executing the next instruction in an instruction register beforethe current instruction is fully executed.
This helps hide the latency of the CPU when executing instruction, though it doesn't reduce the latency of executing an instruction(an instruction which takes 10 clock cycles will still take 10 clock cycles). 
However, it improves the thrpt of the CPU (how much work can be done in x clock cycles)

### Pipeline Hazards
These are anomalies which prevent the CPU from pipelining the next instruction on the next clock cycle

- Structural hazards: occur when two or more instructions need the same part of the CPU
- Data hazards: occur when an instruction is dependent on an operand needed to be computed from a previous instruction. This hazard can prevent OoO execution, 
especially for Read after Write dependencies as reordering can violate commutativity
- Control hazards: occur when the CPU can't determine what instruction to execute next

### Branch Prediction
CPUs don't sit idly by waiting to determine what path of a control flow statement it should execute, rather, they start speculatively executing the branch they believe will be taken.
They track the history per branch of which branch is taken most frequently and which is not and executes a branch based on how probable it is to be executed.  However, CPUs aren't always right, so if after execution, they determine a branch has not been taken, 
they have to flush and restart the instruction pipeline leading to wasted cycles.
This is why branch predictors are basically useless if the probability of executing a branch is 50/50, as at that point, it's basically up to random guess. 
However, skewed probability on which branch should be taken tends to highly favor branch predictors. 

However, all this would be true except that, modern CPU branch predictors are pretty complex, they can pattern match on history on branch outcomes and speculatively execute instructions based on the pattern they've observed.
iterating over an unsorted array to add a value if it fulfills a specific condition can be magnitudes slower than iterating over a sorted array fulfilling that same condition.
Note that iterating over an unsorted array might not be magnitudes times slower, given a large value 'N' and a small value 'M',the array could follow a predefined sequence [M, M, M, N, M, M, M, N].

This pattern matching could allow the branch predictor to make more informed guesses on which branch to execute if based on its history, a branch follows a specific pattern


A more practical example is a JMH benchmark of a thread iterating an array of size 1 << 16 (~65k elements) with values constrained in a set A which allows values [0..99].
In this benchmark we simply iterate over the array and add a value at an index if the value satisfies a simple condition

We test against two things: 
1. An unsorted array: This tests the thrpt of the branch predictor solely based on probability a branch will be taken or not
2. A sorted array: This additionally tests the thrpt of the branch prediction based on its ability to pattern match

```md
Unsorted
Benchmark                    (threshold)   Mode  Cnt      Score     Error  Units
BranchPredictionBench.bench      10  thrpt   20  26221.576 ± 368.361  ops/s
BranchPredictionBench.bench      30  thrpt   20   8464.811 ±  90.114  ops/s
BranchPredictionBench.bench      50  thrpt   20   5498.072 ± 383.516  ops/s
BranchPredictionBench.bench      70  thrpt   20   7115.678 ± 127.504  ops/s
BranchPredictionBench.bench      90  thrpt   20  14666.053 ± 557.519  ops/s


Sorted
Benchmark                    (threshold)   Mode  Cnt      Score      Error  Units
BranchPredictionBench.bench      10  thrpt   20  52431.893 ±  747.425  ops/s
BranchPredictionBench.bench      30  thrpt   20  45153.433 ± 1013.584  ops/s
BranchPredictionBench.bench      50  thrpt   20  39990.820 ±  883.996  ops/s
BranchPredictionBench.bench      70  thrpt   20  35132.726 ±  651.556  ops/s
BranchPredictionBench.bench      90  thrpt   20  32396.280 ±  641.441  ops/s
```

 Based on the empirical data:
1. Unsorted array: The data shows a U shaped graph, with the lowest dip being at the 50 threshold (which is basically 50/50 probability), 
where executing a branch is up to random chance and the high limits being at the 10 and 90 threshold (highly skewed 90/10 probability) that a branch is taken

2. Sorted array: The data shows an inverted linear graph. Here the probability a branch is executed is basically collapsed to 100% as the branch predictor can accurately pattern match and predict
whether a branch should be taken or not. The 50 threshold is not the lowest dip here, rather the 90 threshold is. This loss in thrpt is due to the fact as the threshold increases, more elements
satisfy the branch condition, which increases as the threshold increases, hence more work is done, leading to less thrpt. I tested this by flipping the branch condition and the numbers shifted according to the number of elements
the condition satisfied