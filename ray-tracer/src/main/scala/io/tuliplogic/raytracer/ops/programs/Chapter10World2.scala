package io.tuliplogic.raytracer.ops.programs

import java.nio.file.{Path, Paths}

import io.tuliplogic.raytracer.commons.errors.{AlgebraicError, RayTracerError}
import io.tuliplogic.raytracer.geometry.matrix.MatrixOps
import io.tuliplogic.raytracer.geometry.vectorspace.PointVec.{Pt, Vec}
import io.tuliplogic.raytracer.geometry.vectorspace.{AffineTransformation, AffineTransformationOps}
import io.tuliplogic.raytracer.ops.drawing.Scene.RichRayOperations
import io.tuliplogic.raytracer.ops.drawing.{Camera, Pattern, Renderer, ViewTransform, World}
import io.tuliplogic.raytracer.ops.model.SpatialEntity.SceneObject
import io.tuliplogic.raytracer.ops.model.SpatialEntity.SceneObject.{Plane, PointLight, Sphere}
import io.tuliplogic.raytracer.ops.model.{Canvas, Color, Material, PhongReflection, RayOperations, SpatialEntityOperations}
import io.tuliplogic.raytracer.ops.rendering.{canvasRendering, CanvasRenderer}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.{console, App, UIO, ZIO}
import zio.ZEnv


object Chapter10World2 extends App {
  val canvasFile    = "/tmp/nioexp/chapter-10-two-spheres-shadow-" + System.currentTimeMillis + ".ppm"
  val lightPosition = Pt(10, 10, 2)
  val cameraFrom    = Pt(12, 4, 0)
  val cameraTo      = Pt(0, 4, 16)
  val cameraUp      = Vec(0, 1, 0)

  val (hRes, vRes) = (640, 480)
//  val (hRes, vRes) = (100, 50)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    program
      .provide {
        new CanvasRenderer.PPMCanvasRenderer with RichRayOperations.Live with Blocking.Live with MatrixOps.Live with Console.Live with Clock.Live
        with AffineTransformationOps.Live {
          override def path: Path = Paths.get(canvasFile)
        }
      }
      .foldM(err => console.putStrLn(s"Execution failed with: ${err.getStackTraceString}").as(1), _ => UIO.succeed(0))

  //TODO: make a DSL to build a world, this is too painful

  val world: ZIO[AffineTransformationOps, AlgebraicError, World] = for {
    mat      <- Material.default
    idTf     <- AffineTransformation.id
    floorMat <- UIO(mat.copy(pattern = Pattern.Checker(Color(1, 0.9, 0.9), Color(0.9, 1, 0.9), idTf), specular = 0))
//    floorMat <- UIO(mat.copy(pattern = Pattern.Ring(Color(1, 0.9, 0.9), Color(0.9, 1, 0.9), idTf), specular = 0))
    floorS <- Plane.canonical.map(_.copy(material = floorMat)) //grey, matte

    leftWallTf2 <- AffineTransformation.rotateX(math.Pi / 2)
    leftWallTf3 <- AffineTransformation.rotateY(-math.Pi / 2)
    leftWallTf4 <- AffineTransformation.translate(-10, 0, 0)
    leftWallTf  <- (leftWallTf2 >=> leftWallTf3).flatMap(_ >=> leftWallTf4)
    leftWallS   <- UIO(Plane(leftWallTf, floorMat))

    s1Tf1 <- AffineTransformation.translate(5, 2, 5)
    s1Tf2 <- AffineTransformation.scale(2, 2, 2)
//    p1Tf <- AffineTransformation.scale(0.2, 0.2, 0.2)
    s1Tf <- s1Tf2 >=> s1Tf1
    s1 <- UIO(
      Sphere(
        s1Tf,
        mat.copy(
          pattern = Pattern.GradientX(Color(240 / 256.0, 121 / 256.0, 49 / 256.0), Color(145 / 256.0, 179 / 256.0, 87 / 256.0), idTf),
          diffuse = 0.7,
          specular = 0.3)))

    s2Tf1 <- AffineTransformation.translate(10, 4, 8)
    s2Tf2 <- AffineTransformation.scale(3, 3, 3)
    s2Tf  <- s2Tf2 >=> s2Tf1
    p2Tf1 <- AffineTransformation.scale(0.3, 0.3, 0.3)
    p2Tf2 <- AffineTransformation.rotateZ(math.Pi / 4)
    p2Tf  <- p2Tf1 >=> p2Tf2
    s2    <- UIO(Sphere(s2Tf, mat.copy(pattern = Pattern.Striped(Color.blue, Color.red, p2Tf), diffuse = 0.7, specular = 0.3)))

  } yield World(PointLight(lightPosition, Color.white), List[SceneObject](s1, s2, floorS, leftWallS))

  val camera: ZIO[AffineTransformationOps, AlgebraicError, Camera] = for {
    cameraTf <- ViewTransform(cameraFrom, cameraTo, cameraUp).tf
  } yield Camera(hRes, vRes, math.Pi / 2, cameraTf)

  val program: ZIO[PhongReflection with SpatialEntityOperations with RayOperations with AffineTransformationOps with CanvasRenderer, RayTracerError, Unit] =
    for {
      canvas <- Canvas.create(hRes, vRes)
      wrld   <- world
      cam    <- camera
      _ <- Renderer.render(cam, wrld).flattenChunks.foreach {
        case (px, py, color) =>
          canvas.update(px, py, color)
      }
      _ <- canvasRendering.render(canvas, 255)
    } yield ()

}
