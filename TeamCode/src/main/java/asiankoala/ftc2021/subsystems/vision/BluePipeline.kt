package asiankoala.ftc2021.subsystems.vision

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvPipeline

class BluePipeline : KoawaPipeline() {
    override fun processFrame (input : Mat) : Mat {
        input.copyTo(workingMatrix)

        if (workingMatrix.empty()) {
            return input
        }

        val matLeft = workingMatrix.submat(239, 240, 319, 320)
        val matCenter = workingMatrix.submat(180, 230, 45, 105)
        val matRight = workingMatrix.submat(180, 230, 210, 270)

        LeftTotal =  Core.mean(matLeft).`val`[2]
        CenterTotal = Core.mean(matCenter).`val`[2]
        RightTotal = Core.mean(matRight).`val`[2]

        Imgproc.cvtColor(workingMatrix, workingMatrix, Imgproc.COLOR_RGB2YCrCb)

        Imgproc.rectangle(workingMatrix, Rect(319, 239, 1, 1), Scalar(0.0, 255.0, 0.0))
        Imgproc.rectangle(workingMatrix, Rect(45, 180, 60, 50), Scalar(255.0, 0.0, 0.0))
        Imgproc.rectangle(workingMatrix, Rect(210, 180, 60, 50), Scalar(255.0, 0.0, 0.0))


        return workingMatrix
    }
}
