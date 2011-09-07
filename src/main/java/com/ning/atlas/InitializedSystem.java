package com.ning.atlas;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class InitializedSystem extends InitializedTemplate
{
    private final List<? extends InitializedTemplate> children;

    public InitializedSystem(String type, String name, My my, List<? extends InitializedTemplate> children)
    {
        super(type, name, my);
        this.children = Lists.newArrayList(children);
    }

    @Override
    public Collection<? extends InitializedTemplate> getChildren()
    {
        return children;
    }

    @Override
    public ListenableFuture<? extends InstalledElement> install(Executor ex, InitializedTemplate root)
    {
        final AtomicInteger remaining = new AtomicInteger(getChildren().size());
        final List<InstalledElement> init_children = new CopyOnWriteArrayList<InstalledElement>();
        final SettableFuture<InstalledElement> rs = SettableFuture.create();
        for (InitializedTemplate template : getChildren()) {
            final ListenableFuture<? extends InstalledElement> child = template.install(ex, root);
            child.addListener(new Runnable()
                              {
                                  @Override
                                  public void run()
                                  {
                                      try {
                                          final InstalledElement ct = child.get();
                                          init_children.add(ct);
                                          if (remaining.decrementAndGet() == 0) {
                                              rs.set(new InstalledSystem(getType(),
                                                                                   getName(),
                                                                                   getMy(),
                                                                                   init_children));
                                          }
                                      }
                                      catch (InterruptedException e) {
                                          e.printStackTrace();
                                          System.exit(1);
                                      }
                                      catch (ExecutionException e) {
                                          e.printStackTrace();
                                          System.exit(1);
                                      }
                                  }
                              }, ex);
        }
        return rs;
    }
}
