package io.github.kusoradeolu.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableStackTest {
    @Test
    public void push_topElement_shouldEqualValue(){
        ImmutableStack<Integer> stack = ImmutableStack.empty();
        stack = stack.push(1);
        assertEquals(1, stack.value());
    }

    @Test
    public void pop_topElement_ifEmpty_shouldEqualNull(){
        ImmutableStack<Integer> stack = ImmutableStack.empty();
        stack = stack.push(1);
        stack = stack.pop();
        assertNull(stack.value());
    }

    @Test
    public void pop_topElement_shouldEqualNext(){
        ImmutableStack<Integer> stack = ImmutableStack.empty();
        stack = stack.push(1);
        stack = stack.push(2);
        stack = stack.pop();
        assertEquals(1, stack.value());
    }

    @Test
    public void peek_topElement_shouldEqualStackHead(){
        ImmutableStack<Integer> stack = ImmutableStack.empty();
        stack = stack.push(1);
        stack = stack.push(2);
        assertEquals(2, stack.value());
    }

    @Test
    public void size_ifEmpty_shouldEqualZero(){
        ImmutableStack<Integer> stack = ImmutableStack.empty();
        assertEquals(0, stack.size());
    }

    @Test
    public void size_shouldRecursivelyGetSize(){
        ImmutableStack<Integer> stack = ImmutableStack.empty();
        stack = stack.push(1);
        stack = stack.push(2);
        assertEquals(2, stack.size());
    }
}