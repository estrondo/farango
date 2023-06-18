package one.estrondo.farango

import scala.reflect.ClassTag

inline def typeOf[A: ClassTag]: Class[A] = summon[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]]
