package com.example.barcode.analyzer

import android.graphics.RectF
import com.example.barcode.model.BarCodeResult
import com.example.barcode.model.CoordinatesTransformUiModel
import com.example.barcode.model.ScanningResult
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeResultBoundaryAnalyzer {
    private var barcodeScanningAreaRect: RectF = RectF()
    private var cameraBoundaryRect: RectF = RectF()
    private val barcodeSizeRateThreshold = 0.1F

    fun onBarcodeScanningAreaReady(scanningArea: RectF) {
        barcodeScanningAreaRect = scanningArea
    }

    fun onCameraBoundaryReady(cameraBoundary: RectF) {
        cameraBoundaryRect = cameraBoundary
    }

    fun analyze(
        results: List<Barcode>,
        inputImage: InputImage
    ): ScanningResult {
        results.forEach { barcode ->
            val transformInfo =
                getTransformInfo(
                    cameraBoundary = cameraBoundaryRect,
                    capturedImageBoundary =
                        RectF(
                            0F,
                            0F,
                            inputImage.width.toFloat(),
                            inputImage.height.toFloat()
                        ),
                    imageRotationDegree = inputImage.rotationDegrees
                )
            val transformedPosition =
                transformBarcodeBoundaryToGlobalPosition(
                    barcode = barcode,
                    transformUiModel = transformInfo
                )
            return generateScanningResultByBoundary(transformedPosition, barcode)
        }
        return ScanningResult.Initial()
    }

    private fun transformBarcodeBoundaryToGlobalPosition(
        barcode: Barcode,
        transformUiModel: CoordinatesTransformUiModel
    ): RectF {
        val barcodeBoundary = barcode.boundingBox
        return when (barcodeBoundary?.isEmpty) {
            false -> {
                RectF(
                    transformUiModel.scaleX *
                        (barcodeBoundary.left.toFloat() - transformUiModel.offsetX),
                    transformUiModel.scaleY *
                        (barcodeBoundary.top.toFloat() - transformUiModel.offsetY),
                    transformUiModel.scaleX *
                        (barcodeBoundary.right.toFloat() + transformUiModel.offsetX),
                    transformUiModel.scaleY *
                        (barcodeBoundary.bottom.toFloat() + transformUiModel.offsetY)
                )
            }
            else -> RectF()
        }
    }

    private fun getTransformInfo(
        cameraBoundary: RectF,
        capturedImageBoundary: RectF,
        imageRotationDegree: Int
    ): CoordinatesTransformUiModel {
        return when ((imageRotationDegree / 90) % 2) {
            0 -> { // 0, 180, 360
                val scaleX = cameraBoundary.width() / capturedImageBoundary.width()
                val scaleY = cameraBoundary.height() / capturedImageBoundary.height()
                CoordinatesTransformUiModel(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    offsetX = 0F,
                    offsetY = 50F
                )
            }
            1 -> { // 90, 270
                val scaleX = cameraBoundary.width() / capturedImageBoundary.height()
                val scaleY = cameraBoundary.height() / capturedImageBoundary.width()
                CoordinatesTransformUiModel(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    offsetX = 50F,
                    offsetY = 0F
                )
            }
            else -> CoordinatesTransformUiModel()
        }
    }

    private fun generateScanningResultByBoundary(
        barcodeGlobalPosition: RectF,
        barcode: Barcode
    ): ScanningResult {
        return when {
            checkRectangleInside(
                barcodeGlobalPosition,
                barcodeScanningAreaRect
            ) -> {
                if (checkDistanceMatched(barcodeGlobalPosition, barcodeScanningAreaRect)) {
                    // Perfect match
                    ScanningResult.PerfectMatch(
                        barCodeResult =
                            BarCodeResult(
                                barCode = barcode,
                                globalPosition = barcodeGlobalPosition
                            )
                    )
                } else {
                    // Move closer
                    ScanningResult.InsideBoundary(
                        barCodeResult =
                            BarCodeResult(
                                barCode = barcode,
                                globalPosition = barcodeGlobalPosition
                            )
                    )
                }
            }
            !checkRectangleNotOverlap(barcodeGlobalPosition, barcodeScanningAreaRect) -> {
                ScanningResult.BoundaryOverLap(
                    barCodeResult =
                        BarCodeResult(
                            barCode = barcode,
                            globalPosition = barcodeGlobalPosition
                        )
                )
            }
            else -> {
                ScanningResult.OutOfBoundary()
            }
        }
    }

    /*
     * Cond1. If A's left edge is to the right of the B's right edge,
     *        then A is Totally to right Of B
     * Cond2. If A's right edge is to the left of the B's left edge,
     *        then A is Totally to left Of B
     * Cond3. If A's top edge is below B's bottom edge, - then A is Totally below B
     * Cond4. If A's bottom edge is above B's top edge, - then A is Totally above B
     *
     * NON-Overlap => Cond1 Or Cond2 Or Cond3 Or Cond4
     * Overlap => NOT (Cond1 Or Cond2 Or Cond3 Or Cond4)
     */
    private fun checkRectangleNotOverlap(
        areaOne: RectF,
        areaTwo: RectF
    ): Boolean {
        return areaTwo.left >= areaOne.right || areaTwo.right < areaOne.left ||
            areaTwo.top > areaOne.bottom || areaTwo.bottom < areaOne.top
    }

    private fun checkRectangleInside(
        smallArea: RectF,
        largeArea: RectF
    ): Boolean {
        return smallArea.left > largeArea.left &&
            smallArea.top > largeArea.top &&
            smallArea.right < largeArea.right &&
            smallArea.bottom < largeArea.bottom
    }

    private fun checkDistanceMatched(
        smallArea: RectF,
        largeArea: RectF
    ): Boolean {
        val rate =
            (smallArea.width() * smallArea.height()) /
                (largeArea.width() * largeArea.height())
        return rate > barcodeSizeRateThreshold
    }
}