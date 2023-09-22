package nullterminator.metamodel.factory;

import ro.lrg.xcore.metametamodel.XEntity;
import nullterminator.metamodel.entity.*;
import nullterminator.metamodel.impl.*;

public class Factory {
   protected static Factory singleInstance = new Factory();
   public static Factory getInstance() { return singleInstance;}
   protected Factory() {}
   private LRUCache<Object, XEntity> lruCache_ = new LRUCache<>(1000);
   public void setCacheCapacity(int capacity) {
       lruCache_.setCapacity(capacity);
   }
   public void clearCache() {lruCache_.clear();}
   public MethodInvocationModel createMethodInvocationModel(java.lang.Object obj) {
       XEntity instance = lruCache_.get(obj);
        if (null == instance) {
           instance = new MethodInvocationModelImpl(obj);
           lruCache_.put(obj, instance);
        }
        return (MethodInvocationModel)instance;
    }
   public CompUnitModel createCompUnitModel(java.lang.Object obj) {
       XEntity instance = lruCache_.get(obj);
        if (null == instance) {
           instance = new CompUnitModelImpl(obj);
           lruCache_.put(obj, instance);
        }
        return (CompUnitModel)instance;
    }
   public CheckedElementModel createCheckedElementModel(java.lang.Object obj) {
       XEntity instance = lruCache_.get(obj);
        if (null == instance) {
           instance = new CheckedElementModelImpl(obj);
           lruCache_.put(obj, instance);
        }
        return (CheckedElementModel)instance;
    }
   public ProjectModel createProjectModel(java.lang.Object obj) {
       XEntity instance = lruCache_.get(obj);
        if (null == instance) {
           instance = new ProjectModelImpl(obj);
           lruCache_.put(obj, instance);
        }
        return (ProjectModel)instance;
    }
}
