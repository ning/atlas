package com.ning.atlas;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ProvisionedSystemTemplate extends ProvisionedTemplate
{
    private List<? extends ProvisionedTemplate> children;

    public ProvisionedSystemTemplate(String type, String name, List<? extends ProvisionedTemplate> children)
    {
        super(type, name);
        this.children = new ArrayList<ProvisionedTemplate>(children);
    }

    public List<? extends ProvisionedTemplate> getChildren()
    {
        return children;
    }

    @Override
    public ListenableFuture<InitializedTemplate> initialize(Executor ex, ProvisionedTemplate root)
    {
        final AtomicInteger remaining = new AtomicInteger(getChildren().size());
        final List<InitializedTemplate> init_children = new CopyOnWriteArrayList<InitializedTemplate>();
        final SettableFuture<InitializedTemplate> rs = SettableFuture.create();
        for (ProvisionedTemplate template : getChildren()) {
            final ListenableFuture<? extends InitializedTemplate> child = template.initialize(ex, root);
            child.addListener(new Runnable()
                              {
                                  @Override
                                  public void run()
                                  {
                                      try {
                                          final InitializedTemplate ct = child.get();
                                          init_children.add(ct);
                                          if (remaining.decrementAndGet() == 0) {
                                              rs.set(new InitializedSystemTemplate(getType(), getType(), init_children));
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
