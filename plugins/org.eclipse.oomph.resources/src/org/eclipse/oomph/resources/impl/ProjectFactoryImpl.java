/**
 */
package org.eclipse.oomph.resources.impl;

import org.eclipse.oomph.base.impl.ModelElementImpl;
import org.eclipse.oomph.resources.ProjectFactory;
import org.eclipse.oomph.resources.ResourcesPackage;
import org.eclipse.oomph.resources.backend.BackendContainer;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Project Factory</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.oomph.resources.impl.ProjectFactoryImpl#getExcludedPaths <em>Excluded Paths</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class ProjectFactoryImpl extends ModelElementImpl implements ProjectFactory
{
  /**
   * The cached value of the '{@link #getExcludedPaths() <em>Excluded Paths</em>}' attribute list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getExcludedPaths()
   * @generated
   * @ordered
   */
  protected EList<String> excludedPaths;

  private Set<String> excludedPathSet;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ProjectFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return ResourcesPackage.Literals.PROJECT_FACTORY;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  public EList<String> getExcludedPaths()
  {
    if (excludedPaths == null)
    {
      excludedPaths = new EDataTypeUniqueEList<String>(String.class, this, ResourcesPackage.PROJECT_FACTORY__EXCLUDED_PATHS)
      {
        private static final long serialVersionUID = 1L;

        @Override
        protected void didChange()
        {
          synchronized (ProjectFactoryImpl.this)
          {
            excludedPathSet = null;
          }
        }
      };
    }
    return excludedPaths;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  public final IProject createProject(BackendContainer backendContainer, IProgressMonitor monitor)
  {
    if (isExcludedPath(backendContainer))
    {
      return null;
    }

    return doCreateProject(backendContainer, monitor);
  }

  protected abstract IProject doCreateProject(BackendContainer backendContainer, IProgressMonitor monitor);

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  public synchronized boolean isExcludedPath(BackendContainer backendContainer)
  {
    if (excludedPathSet == null)
    {
      excludedPathSet = new HashSet<String>(getExcludedPaths());
    }

    String systemRelativePath = backendContainer.getSystemRelativePath();
    return excludedPathSet.contains(systemRelativePath);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case ResourcesPackage.PROJECT_FACTORY__EXCLUDED_PATHS:
        return getExcludedPaths();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case ResourcesPackage.PROJECT_FACTORY__EXCLUDED_PATHS:
        getExcludedPaths().clear();
        getExcludedPaths().addAll((Collection<? extends String>)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case ResourcesPackage.PROJECT_FACTORY__EXCLUDED_PATHS:
        getExcludedPaths().clear();
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case ResourcesPackage.PROJECT_FACTORY__EXCLUDED_PATHS:
        return excludedPaths != null && !excludedPaths.isEmpty();
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException
  {
    switch (operationID)
    {
      case ResourcesPackage.PROJECT_FACTORY___CREATE_PROJECT__BACKENDCONTAINER_IPROGRESSMONITOR:
        return createProject((BackendContainer)arguments.get(0), (IProgressMonitor)arguments.get(1));
      case ResourcesPackage.PROJECT_FACTORY___IS_EXCLUDED_PATH__BACKENDCONTAINER:
        return isExcludedPath((BackendContainer)arguments.get(0));
    }
    return super.eInvoke(operationID, arguments);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy())
    {
      return super.toString();
    }

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (excludedPaths: ");
    result.append(excludedPaths);
    result.append(')');
    return result.toString();
  }

} // ProjectFactoryImpl