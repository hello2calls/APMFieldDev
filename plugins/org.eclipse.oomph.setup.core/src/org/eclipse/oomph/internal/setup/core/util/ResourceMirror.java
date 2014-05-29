/*
 * Copyright (c) 2014 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.oomph.internal.setup.core.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Eike Stepper
 */
public class ResourceMirror
{
  private static final Comparator<LoadJob> COMPARATOR = new Comparator<LoadJob>()
  {
    public int compare(LoadJob o1, LoadJob o2)
    {
      int result = (o2.secondary ? 0 : 1) - (o1.secondary ? 0 : 1);
      if (result == 0)
      {
        result = o2.id - o1.id;
      }

      return result;
    }
  };

  private ResourceSet resourceSet;

  private final Map<URI, LoadJob> loadJobs = new HashMap<URI, LoadJob>();

  private final List<LoadJob> pendingLoadJobs = new ArrayList<LoadJob>();

  private final int maxJobs = 10;

  private int nextJobID;

  private CountDownLatch latch;

  private boolean isCanceled;

  private DelegatingResourceLocator resourceLocator;

  public ResourceMirror()
  {
    this(EMFUtil.createResourceSet());
  }

  public ResourceMirror(ResourceSet resourceSet)
  {
    this.resourceSet = resourceSet;
    resourceLocator = new DelegatingResourceLocator((ResourceSetImpl)resourceSet);
  }

  public ResourceSet getResourceSet()
  {
    return resourceSet;
  }

  public void dispose()
  {
    resourceSet = null;
    resourceLocator.dispose();
  }

  public synchronized void cancel()
  {
    isCanceled = true;

    for (LoadJob loadJob : loadJobs.values())
    {
      loadJob.cancel();
    }

    loadJobs.clear();
    pendingLoadJobs.clear();
  }

  public boolean isCanceled()
  {
    return isCanceled;
  }

  public void mirror(Collection<? extends URI> uris)
  {
    if (latch == null)
    {
      latch = new CountDownLatch(1);
    }

    if (schedule(uris))
    {
      try
      {
        latch.await();
      }
      catch (InterruptedException ex)
      {
        throw new RuntimeException(ex);
      }
    }

    latch = null;
  }

  public void mirror(URI... uris)
  {
    if (latch == null)
    {
      latch = new CountDownLatch(1);
    }

    if (schedule(uris))
    {
      try
      {
        latch.await();
      }
      catch (InterruptedException ex)
      {
        throw new RuntimeException(ex);
      }
    }

    latch = null;
  }

  public void mirror(URI uri)
  {
    if (latch == null)
    {
      latch = new CountDownLatch(1);
    }

    if (schedule(uri))
    {
      try
      {
        latch.await();
      }
      catch (InterruptedException ex)
      {
        throw new RuntimeException(ex);
      }
    }

    latch = null;
  }

  public boolean schedule(Collection<? extends URI> uris)
  {
    boolean result = false;
    for (URI uri : uris)
    {
      if (schedule(uri))
      {
        result = true;
      }
    }

    return result;
  }

  public boolean schedule(URI... uris)
  {
    boolean result = false;
    for (URI uri : uris)
    {
      if (schedule(uri))
      {
        result = true;
      }
    }

    return result;
  }

  public boolean schedule(URI uri)
  {
    return schedule(uri, false);
  }

  private synchronized boolean schedule(URI uri, boolean secondary)
  {
    if (isCanceled())
    {
      return false;
    }

    synchronized (resourceSet)
    {
      Resource resource = resourceSet.getResource(uri, false);
      if (resource != null && resource.isLoaded())
      {
        return false;
      }
    }

    LoadJob loadJob = loadJobs.get(uri);
    if (loadJob != null)
    {
      if (!secondary && loadJob.secondary && pendingLoadJobs.contains(loadJob))
      {
        loadJob.secondary = false;
        Collections.sort(pendingLoadJobs, COMPARATOR);
      }
    }
    else
    {
      loadJob = new LoadJob(uri, ++nextJobID, secondary);
      loadJobs.put(loadJob.uri, loadJob);

      if (isLoadPossible())
      {
        loadJob.schedule();
      }
      else
      {
        pendingLoadJobs.add(loadJob);
        Collections.sort(pendingLoadJobs, COMPARATOR);
      }
    }

    return true;
  }

  private synchronized void deschedule(URI uri)
  {
    loadJobs.remove(uri);

    if (!isCanceled() && !pendingLoadJobs.isEmpty())
    {
      LoadJob loadJob = pendingLoadJobs.remove(0);
      loadJob.schedule();
    }

    if (latch != null && loadJobs.isEmpty())
    {
      latch.countDown();
    }
  }

  private boolean isLoadPossible()
  {
    int all = loadJobs.size() - 1;
    int pending = pendingLoadJobs.size();
    int running = all - pending;
    return running < maxJobs;
  }

  /**
   * @author Eike Stepper
   */
  private final class DelegatingResourceLocator extends ResourceSetImpl.ResourceLocator
  {
    private DelegatingResourceLocator(ResourceSetImpl resourceSet)
    {
      super(resourceSet);
    }

    @Override
    public Resource getResource(URI uri, boolean loadOnDemand)
    {
      if (loadOnDemand && "setup".equals(uri.fileExtension()))
      {
        return null;
      }

      return basicGetResource(uri, loadOnDemand);
    }

    @Override
    public void dispose()
    {
      super.dispose();
    }
  }

  /**
   * @author Eike Stepper
   */
  private class LoadJob extends Job
  {
    private final URI uri;

    private final int id;

    private boolean secondary;

    private LoadJob(URI uri, int id, boolean secondary)
    {
      super("Load " + uri);
      this.uri = uri;
      this.id = id;
      this.secondary = secondary;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
      Resource resource;
      synchronized (resourceSet)
      {
        resource = resourceSet.getResource(uri, false);
        if (resource == null)
        {
          resource = resourceSet.createResource(uri);
        }
      }

      try
      {
        resource.load(resourceSet.getLoadOptions());
      }
      catch (IOException ex)
      {
        new ResourceSetImpl()
        {
          @Override
          public void handleDemandLoadException(Resource resource, IOException exception) throws RuntimeException
          {
            try
            {
              super.handleDemandLoadException(resource, exception);
            }
            catch (RuntimeException ex)
            {
              // Ignore
            }
          }
        }.handleDemandLoadException(resource, ex);
      }

      visit(resource);

      deschedule(uri);

      return Status.OK_STATUS;
    }

    private void delay()
    {
      // try
      // {
      // Thread.sleep(100);
      // }
      // catch (InterruptedException ex)
      // {
      // ex.printStackTrace();
      // }
    }

    private void visit(Resource resource)
    {
      for (Iterator<EObject> it = EcoreUtil.getAllContents(resource, false); it.hasNext();)
      {
        delay();

        if (isCanceled())
        {
          break;
        }

        EObject eObject = it.next();
        if (eObject.eIsProxy())
        {
          URI proxyURI = ((InternalEObject)eObject).eProxyURI().trimFragment();
          ResourceMirror.this.schedule(proxyURI, false);
        }
        else
        {
          for (Iterator<EObject> it2 = ((InternalEList<EObject>)eObject.eCrossReferences()).basicListIterator(); it2.hasNext();)
          {
            delay();

            if (isCanceled())
            {
              break;
            }

            EObject eCrossReference = it2.next();
            if (eCrossReference.eIsProxy())
            {
              URI proxyURI = ((InternalEObject)eCrossReference).eProxyURI().trimFragment();
              ResourceMirror.this.schedule(proxyURI, true);
            }
          }
        }
      }
    }

    @Override
    public String toString()
    {
      return "uri=" + uri + ", secondary=" + secondary + ", id=" + id;
    }
  }
}
