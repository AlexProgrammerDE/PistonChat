package net.pistonmaster.pistonfilter.utils;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentLinkedDeque;

@RequiredArgsConstructor
public class MaxSizeDeque<C> extends ConcurrentLinkedDeque<C> {
  private final int maxSize;

  @Override
  public boolean add(C c) {
    if (size() == maxSize) {
      removeFirst();
    }

    return super.add(c);
  }
}
