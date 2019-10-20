package io.tuliplogic.raytracer.ops.drawing

import io.tuliplogic.raytracer.commons.errors.RayTracerError
import io.tuliplogic.raytracer.geometry.matrix.MatrixModule
import io.tuliplogic.raytracer.geometry.affine.AffineTransformationOps
import io.tuliplogic.raytracer.geometry.affine.PointVec.{Pt, Vec}
import io.tuliplogic.raytracer.ops.OpsTestUtils
import io.tuliplogic.raytracer.ops.model.{Color, PhongReflection, RayOperations, SpatialEntityOperations}
import zio.stream.{Sink, ZStream}
import zio.{DefaultRuntime, IO, Task, UIO}
import org.scalatest.WordSpec
import org.scalatest.Matchers._

class RendererTest extends WordSpec with DefaultRuntime with OpsTestUtils {

  type Env = PhongReflection with SpatialEntityOperations with RayOperations with AffineTransformationOps
  "renderer" should {
    "produce the expected color for the pixel" in {
      unsafeRun {
        (for {
          w    <- WorldTest.defaultWorld
          from <- UIO(Pt(0, 0, -5))
          to   <- UIO(Pt.origin)
          up   <- UIO(Vec(0, 1, 0))
          tf   <- ViewTransform(from, to, up).tf
          c    <- UIO(Camera(11, 11, math.Pi / 2, tf))
          pixels <- Renderer
            .render(c, w)
            .flattenChunks
            .collect {
              case (5, 5, c) => c
            }
            .run(Sink.collectAllN[Color](1))
          _ <- Task.effectTotal { pixels.head should ===(Color(0.38066, 0.47583, 0.2855)) }
        } yield pixels)
          .provide(new RayOperations.BreezeMatrixOps with PhongReflection.BreezeMatrixOps with SpatialEntityOperations.BreezeMatrixOps with MatrixModule.BreezeMatrixModule with AffineTransformationOps.BreezeMatrixOps$)
      }
    }
  }
}
