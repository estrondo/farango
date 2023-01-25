package farango

import scala.reflect.ClassTag

def expectedClass[T: ClassTag]: Class[T] = summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
