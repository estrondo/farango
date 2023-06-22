package one.estrondo.farango.ducktape

import io.github.arainko.ducktape.BuilderConfig
import io.github.arainko.ducktape.Transformer
import one.estrondo.farango
import one.estrondo.farango.FarangoTransformer

class DucktapeTransformer[A, B](transformer: Transformer[A, B]) extends FarangoTransformer[A, B]:

  override def transform(value: A): B = transformer.transform(value)

object DucktapeTransformer:

  inline def apply[A, B](inline config: BuilderConfig[A, B]*): FarangoTransformer[A, B] = new DucktapeTransformer(
    Transformer.define.build(config*)
  )

inline given [A, B]: FarangoTransformer[A, B] = DucktapeTransformer()
