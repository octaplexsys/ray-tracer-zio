package io.tuliplogic.geometry.matrix

import io.tuliplogic.geometry.matrix.Entity3D.{Pt, Vec}
import io.tuliplogic.geometry.matrix.Types.{Col, M}
import io.tuliplogic.raytracer.errors.MatrixError
import zio.{IO, UIO, ZIO}

//TODO: applyAffineTransformations require an implementation of matrix ops
/**
  * A transformation is by construction a 4 x 4 matrix. We need just to validate the vectors that it operates on are 4 x 1
  */
class AffineTransformation private (val m: M) {
  private def on(v: Col): ZIO[MatrixOps, MatrixError, Col] =
    for {
      v_m <- v.m
      v_n <- v.n
      _   <- if (v_m != 4 || v_n != 1) ZIO.fail(MatrixError.MatrixDimError(s"can't apply an affine transformation to a matrix $v_m x $v_n")) else ZIO.unit
      res <- matrixOperations.mul(m, v)

    } yield res

  def on(pt: Pt): ZIO[MatrixOps, MatrixError, Pt] = on(pt.col).map(Pt.apply)
  def on(vec: Vec): ZIO[MatrixOps, MatrixError, Vec] = on(vec.col).map(Vec.apply)

  def >=>(next: AffineTransformation): ZIO[MatrixOps, Nothing, AffineTransformation] =
    matrixOperations.mul(next.m, this.m).map(new AffineTransformation(_)).orDie

  def <=<(next: AffineTransformation): ZIO[MatrixOps, Nothing, AffineTransformation] =
    matrixOperations.mul(this.m, next.m).map(new AffineTransformation(_)).orDie
}

object AffineTransformation {
  import Types._
  import vectorizable.comp

  import math.{cos, sin}
  def composeLeft(tf: AffineTransformation*): ZIO[MatrixOps, Nothing, AffineTransformation] =
    tf.toList.foldLeft(id.provideSome[MatrixOps](r => r)) {
      case (accF, next) =>
        for {
          acc <- accF
          res <- acc >=> next
        } yield res
    }

  /**
    * this makes calculations simpler through matrix multiplication
    * - a point can be translated and moved (it's referred to the origin of the reference frame)
    * - a vector cannot be translated, i.e. if I translate a vector I get the same vector back, as a vector can be seen as always starting from the origin
    */
  def point(x: Double, y: Double, z: Double): UIO[Pt]  = factory.createColVector(comp(x, y, z, 1)).map(Pt.apply)
  def vector(x: Double, y: Double, z: Double): UIO[Vec] = factory.createColVector(comp(x, y, z, 0)).map(Vec.apply)

  def id: UIO[AffineTransformation] = translate(0, 0, 0)

  def translate(x: Double, y: Double, z: Double): UIO[AffineTransformation] =
    factory
      .fromRows(
        4,
        4,
        comp(
          comp(1d, 0d, 0d, x),
          comp(0d, 1d, 0d, y),
          comp(0d, 0d, 1d, z),
          comp(0d, 0d, 0d, 1d)
        ))
      .map(new AffineTransformation(_))
      .orDie

  def scale(x: Double, y: Double, z: Double): UIO[AffineTransformation] =
    factory
      .fromRows(
        4,
        4,
        comp(
          comp(x, 0d, 0d, 0d),
          comp(0d, y, 0d, 0d),
          comp(0d, 0d, z, 0d),
          comp(0d, 0d, 0d, 1d)
        ))
      .map(new AffineTransformation(_))
      .orDie

  def rotateX(θ: Double): UIO[AffineTransformation] =
    factory
      .fromRows(
        4,
        4,
        comp(
          comp(1d, 0d, 0d, 0d),
          comp(0d, cos(θ), -sin(θ), 0d),
          comp(0d, sin(θ), cos(θ), 0d),
          comp(0d, 0d, 0d, 1d)
        ))
      .map(new AffineTransformation(_))
      .orDie

  def rotateY(θ: Double): UIO[AffineTransformation] =
    factory
      .fromRows(
        4,
        4,
        comp(
          comp(cos(θ), 0d, -sin(θ), 0d),
          comp(0d, 1d, 0d, 0d),
          comp(sin(θ), 0d, cos(θ), 0d),
          comp(0d, 0d, 0d, 1d)
        ))
      .map(new AffineTransformation(_))
      .orDie

  def rotateZ(θ: Double): UIO[AffineTransformation] =
    factory
      .fromRows(
        4,
        4,
        comp(
          comp(cos(θ), -sin(θ), 0d, 0d),
          comp(sin(θ), cos(θ), 0d, 0d),
          comp(0d, 0d, 1d, 0d),
          comp(0d, 0d, 0d, 1d)
        ))
      .map(new AffineTransformation(_))
      .orDie

  def shearing(xY: Double, xZ: Double, yX: Double, yZ: Double, zX: Double, zY: Double): UIO[AffineTransformation] =
    factory
      .fromRows(
        4,
        4,
        comp(
          comp(1d, xY, xZ, 0d),
          comp(yX, 1d, yZ, 0d),
          comp(zX, zY, 1d, 0d),
          comp(0d, 0d, 0d, 1d)
        ))
      .map(new AffineTransformation(_))
      .orDie

}
