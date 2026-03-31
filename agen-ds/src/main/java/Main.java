import io.github.kusoradeolu.ads.ImmutableStack;

void main() {
    ImmutableStack<Integer> stack = ImmutableStack.of(1);
    for (int i = 0; i < 100; ++i){
        stack = stack.push(i);
    }

    IO.println(stack.size());
    IO.println(stack.pop());
}