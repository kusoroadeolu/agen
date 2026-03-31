package io.github.kusoradeolu.ads;

import java.util.Objects;

public sealed interface ImmutableStack<E> permits ImmutableStack.EmptyStack, ImmutableStack.NonEmptyStack  {
    EmptyStack<?> EMPTY = new EmptyStack<>();


    //Returns the value of the top of the stack
    E value();

    record EmptyStack<E>() implements ImmutableStack<E>{
        @Override
        public E value() {
            return null;
        }
    }

    record NonEmptyStack<E>(E value, ImmutableStack<E> next) implements ImmutableStack<E>{

    }

    default ImmutableStack<E> push(E value){
        Objects.requireNonNull(value, "Value cannot be null");
        return new NonEmptyStack<>(value, this);
    }

    default ImmutableStack<E> pop(){
        return switch (this){
            case EmptyStack<E> e -> e; //Return the empty stack as is
            case NonEmptyStack<E> nes -> nes.next;
        };
    }

    default ImmutableStack<E> peek(){
        return switch (this){
            case EmptyStack<E> e -> e; //Return the empty stack as is
            case NonEmptyStack<E> nes -> nes;
        };
    }

    default int size(){
        return switch (this){
            case EmptyStack<E> _ -> 0; //Return the empty stack as is
            case NonEmptyStack<E> nes -> 1 + nes.next.size();
        };
    }

    static <E>ImmutableStack<E> of(E value){
        return new NonEmptyStack<>(value, empty());
    }

    @SuppressWarnings("unchecked")
    static <E> ImmutableStack<E> empty(){
        return (ImmutableStack<E>) EMPTY;
    }

}
