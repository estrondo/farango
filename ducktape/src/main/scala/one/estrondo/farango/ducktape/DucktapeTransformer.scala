package one.estrondo.farango.ducktape

import io.github.arainko.ducktape.Transformer
import one.estrondo.farango
import one.estrondo.farango.FarangoTransformer

object DucktapeTransformer:

  def apply[A, B](transformer: Transformer[A, B]): FarangoTransformer[A, B] = new Impl(transformer)

  private class Impl[A, B](transformer: Transformer[A, B]) extends FarangoTransformer[A, B]:

    override def transform(value: A): B = transformer.transform(value)
