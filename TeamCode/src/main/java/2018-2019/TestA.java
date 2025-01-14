package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.FRONT;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

@Autonomous(name = "Blue2", group = "Blue")
public class TestA extends LinearOpMode {
    CloneAutoOpRobot robot = new CloneAutoOpRobot();
    private ElapsedTime runtime = new ElapsedTime();

    private static final String VUFORIA_KEY = "Adt99vT/////AAABmQfaL1Gc6k6YpSV4p0gyJUg3w2FZlS8RqVrXnweJXsLcl6JrGb5Age+Cv4I9IS+9XG2ZMhWR19WkeOkWkrTXMzKjblOZGI0FC/WUj9CXpGB7wTS8qQuNHut0NT3aZzPjx3aNnjfUCmBvwrCcVHgvLBLU460n9TE9Yug17HApyE+ix9xcJ2J5QtVejR9PNm8SNmpFAEyGmSasukJHF0Em7cNrHAsR5MxPSCBAnQA3B2pL5onCRotpWAu0rcOCYfXWrHeVtYAEHzm3GdFRj73cQ3eGFgOCHJSyMC0AhHH0M8cFlltdKc0f08FHioKrXu8OpvLGCTtYMSZ6Jq2we4+keaVEPBZY6U4YBffLK7jQnP1p";

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here
    private static final float mmFTCFieldWidth = (12 * 6);       // the width of the FTC field (from the center point to the outer panels)
    private static final float mmTargetHeight = (6);          // the height of the center of the target image above the floor

    // Select which camera you want use.  The FRONT camera is the one on the same side as the screen.
    // Valid choices are:  BACK or FRONT
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;

    private OpenGLMatrix lastLocation = null;
    private boolean targetVisible = false;

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    boolean FindTarget = false;
    boolean blockMove = true;
    boolean BlueRoverV = false;
    boolean RedFootprintV = false;
    boolean FrontCratersV = false;
    boolean BackSpaceV = false;
    boolean targetFound = false;

    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;

    static final double     COUNTS_PER_MOTOR_REV    = 28 ;   //Neverest motor // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 40 ;     // This is < 1.0 if geared UP, 40 for a 40:1 reduce to 160 rpm
    static final double     WHEEL_DIAMETER_MM   = 100 ;     // For figuring circumference
    static final double     COUNTS_PER_MM         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                                                                (WHEEL_DIAMETER_MM * 3.1415); //3.56507 If you want to move 500mm the position that needs to be set is 3.5607*500= 1782.535
    static final double     DRIVE_SPEED             = 1;
    static final double     TURN_SPEED              = 0.5;
    static final double     STRAFE_SPEED            = 0.5;

    //TODO
    //Test single Path
    //Test a Path with turns
    //Check direction of motors after  config changes are inspected
    //Once all Tests passes, revert to the orginal runOpMode() code
    @Override
    public void runOpMode() {

        boolean isStarted =false;
        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Resetting Encoders");    //
        telemetry.update();

        robot.motorLeftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.motorLeftRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.motorRightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.motorRightRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.motorLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.motorExtend.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.motorLeftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorLeftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        robot.motorRightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorRightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        robot.motorLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorExtend.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

//        // Send telemetry message to indicate successful Encoder reset
//        telemetry.addData("Path0, before",  "Starting at %7d :%7d",
//                robot.motorLeftFront.getCurrentPosition(),
//                robot.motorRightFront.getCurrentPosition());
        telemetry.addData("Positions at Start, before, LF,LR,RF,RR,LIFT,EXT",  "Starting at %7d :%7d: %7d :%7d %7d :%7d",
                robot.motorLeftFront.getCurrentPosition(),robot.motorLeftRear.getCurrentPosition(),
                robot.motorRightFront.getCurrentPosition(),robot.motorRightRear.getCurrentPosition(),
                robot.motorLift.getCurrentPosition(),
                robot.motorExtend.getCurrentPosition());
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        //NOTE From https://ftcforum.usfirst.org/forum/ftc-technology/android-studio/66556-encoders-not-reading-correctly
        //Add the following line to Test the encoders, getting values from them
        //*****************************************************************************************************************
        //1:40 motor should do 28*40 = 1120 for full Rotation
//        while(!isStarted){
//            telemetry.addData("Test Counts for debugging",  "Running at LF,LR,RF,RR,LIFT,EXT\",  \"Starting at %7d :%7d: %7d :%7d %7d :%7d\"",
//                    robot.motorLeftFront.getCurrentPosition(),
//                    robot.motorRightFront.getCurrentPosition(),
//                    robot.motorLeftRear.getCurrentPosition(),
//                    robot.motorRightRear.getCurrentPosition(),
//                    robot.motorLift.getCurrentPosition(),
//                    robot.motorExtend.getCurrentPosition());
//            telemetry.update();
//        }
        //*****************************************************************************************************************
        //Forward 500 mm
        encoderDriveForwardorBackwards(DRIVE_SPEED,500,5);
        //Turn left
        encoderTurn(TURN_SPEED,-150,150,5);
        //Reverse 500 mm
        encoderDriveForwardorBackwards(DRIVE_SPEED,-500,5);

        //Turn right
        encoderTurn(TURN_SPEED,150,-+150,5);
        //Forward 500 mm
        encoderDriveForwardorBackwards(DRIVE_SPEED,250,5);

        //Drop the Beacon
        dropBeacon(5);


        //turn left again (90 Degrees Left)
        encoderTurn(TURN_SPEED,-450,450,5);

        encoderDriveForwardorBackwards(DRIVE_SPEED,250,5);

        //Turn 90 Degrees Right
        encoderTurn(TURN_SPEED,450,-450,5);

        //Forward 500 mm
        encoderDriveForwardorBackwards(DRIVE_SPEED,1000,5);

        //This moves it about a Third down, experiment to get the full extend
        encoderMoveLift(-2000,0.5,2);

        //This moves it about a Third down, experiment to get the full extend
        encoderExtender(-2000,0.5,3);

        encoderMoveLift(2000,0.5,2);

        encoderExtender(2000,0.5,3);
        //Sideways 500mm right
        encoderStrafe(STRAFE_SPEED, 500, -500, 5);
        //Sideways 1000mm left
        encoderStrafe(STRAFE_SPEED, -1000, 1000, 5);


        telemetry.addData("Path", "Complete");
        telemetry.update();
    }

    //Note ******************************************************************************************
    //  Uncomment this code for Comp if the aboth Test are prove that the Robot config are correct
    //************************************************************************************************
//    @Override
//    public void runOpMode() throws InterruptedException {
//
//        if (blockMove == false) {
//            initVuforia();
//
//            if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
//                initTfod();
//            } else {
//                telemetry.addData("Sorry!", "This device is not compatible with TFOD");
//            }
//
//            /** Wait for the game to begin */
//            telemetry.addData(">", "Press Play to start tracking");
//            telemetry.update();
//            waitForStart();
//
//            robot.init(hardwareMap);
//
//            robot.motorLeftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//            robot.motorLeftRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//            robot.motorRightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//            robot.motorRightRear.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//
//            telemetry.addData("Path0",  "Starting at %7d :%7d",
//                    robot.motorLeftFront.getCurrentPosition(),
//                    robot.motorLeftRear.getCurrentPosition(),
//                    robot.motorRightFront.getCurrentPosition(),
//                    robot.motorRightRear.getCurrentPosition());
//            telemetry.update();
//
//            while (opModeIsActive()) {
//
//                robot.lower();
//                telemetry.addLine("Lower");
//                telemetry.update();
//
//                /** Activate Tensor Flow Object Detection. */
//                if (tfod != null) {
//                    tfod.activate();
//                }
//
//                while (opModeIsActive()) {
//                    if (tfod != null) {
//                        // getUpdatedRecognitions() will return null if no new information is available since
//                        // the last time that call was made.
//                        List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
//                        if (updatedRecognitions != null) {
//                            telemetry.addData("# Object Detected", updatedRecognitions.size());
//                            if (updatedRecognitions.size() == 3) {
//                                int goldMineralX = -1;
//                                int silverMineral1X = -1;
//                                int silverMineral2X = -1;
//                                for (Recognition recognition : updatedRecognitions) {
//                                    if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
//                                        goldMineralX = (int) recognition.getLeft();
//                                    } else if (silverMineral1X == -1) {
//                                        silverMineral1X = (int) recognition.getLeft();
//                                    } else {
//                                        silverMineral2X = (int) recognition.getLeft();
//                                    }
//                                }
//                                if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
//                                    if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
//                                        telemetry.addData("Gold Mineral Position", "Left");
//                                        robot.MoveL();
//                                        robot.Sample();
//                                        robot.ResetLeft();
//                                        blockMove = true;
//                                    } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
//                                        telemetry.addData("Gold Mineral Position", "Right");
//                                        robot.MoveR();
//                                        robot.Sample();
//                                        robot.ResetRight();
//                                        blockMove = true;
//                                    } else {
//                                        telemetry.addData("Gold Mineral Position", "Center");
//                                        robot.Sample();
//                                        robot.ResetMid();
//                                        blockMove = true;
//                                    }
//                                }
//                            }
//                            telemetry.update();
//                        }
//                    }
//                }
//            }
//        }
//        if (blockMove == true && FindTarget == false) {
//            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
//            VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
//
//            parameters.vuforiaLicenseKey = VUFORIA_KEY;
//            parameters.cameraDirection = CAMERA_CHOICE;
//
//            vuforia = ClassFactory.getInstance().createVuforia(parameters);
//
//            VuforiaTrackables targetsRoverRuckus = this.vuforia.loadTrackablesFromAsset("RoverRuckus");
//            VuforiaTrackable blueRover = targetsRoverRuckus.get(0);
//            blueRover.setName("Blue-Rover");
//            VuforiaTrackable redFootprint = targetsRoverRuckus.get(1);
//            redFootprint.setName("Red-Footprint");
//            VuforiaTrackable frontCraters = targetsRoverRuckus.get(2);
//            frontCraters.setName("Front-Craters");
//            VuforiaTrackable backSpace = targetsRoverRuckus.get(3);
//            backSpace.setName("Back-Space");
//
//            List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();
//            allTrackables.addAll(targetsRoverRuckus);
//
//            /**
//             * If you are standing in the Red Alliance Station looking towards the center of the field,
//             *     - The X axis runs from your left to the right. (positive from the center to the right)
//             *     - The Y axis runs from the Red Alliance Station towards the other side of the field
//             *       where the Blue Alliance Station is. (Positive is from the center, towards the BlueAlliance station)
//             *     - The Z axis runs from the floor, upwards towards the ceiling.  (Positive is above the floor)
//             */
//
//            /**
//             * To place the BlueRover target in the middle of the blue perimeter wall:
//             * - First we rotate it 90 around the field's X axis to flip it upright.
//             * - Then, we translate it along the Y axis to the blue perimeter wall.
//             */
//            OpenGLMatrix blueRoverLocationOnField = OpenGLMatrix
//                    .translation(0, mmFTCFieldWidth, mmTargetHeight)
//                    .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0));
//            blueRover.setLocation(blueRoverLocationOnField);
//
//            /**
//             * To place the RedFootprint target in the middle of the red perimeter wall:
//             * - First we rotate it 90 around the field's X axis to flip it upright.
//             * - Second, we rotate it 180 around the field's Z axis so the image is flat against the red perimeter wall
//             *   and facing inwards to the center of the field.
//             * - Then, we translate it along the negative Y axis to the red perimeter wall.
//             */
//            OpenGLMatrix redFootprintLocationOnField = OpenGLMatrix
//                    .translation(0, -mmFTCFieldWidth, mmTargetHeight)
//                    .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180));
//            redFootprint.setLocation(redFootprintLocationOnField);
//
//            /**
//             * To place the FrontCraters target in the middle of the front perimeter wall:
//             * - First we rotate it 90 around the field's X axis to flip it upright.
//             * - Second, we rotate it 90 around the field's Z axis so the image is flat against the front wall
//             *   and facing inwards to the center of the field.
//             * - Then, we translate it along the negative X axis to the front perimeter wall.
//             */
//            OpenGLMatrix frontCratersLocationOnField = OpenGLMatrix
//                    .translation(-mmFTCFieldWidth, 0, mmTargetHeight)
//                    .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90));
//            frontCraters.setLocation(frontCratersLocationOnField);
//
//            /**
//             * To place the BackSpace target in the middle of the back perimeter wall:
//             * - First we rotate it 90 around the field's X axis to flip it upright.
//             * - Second, we rotate it -90 around the field's Z axis so the image is flat against the back wall
//             *   and facing inwards to the center of the field.
//             * - Then, we translate it along the X axis to the back perimeter wall.
//             */
//            OpenGLMatrix backSpaceLocationOnField = OpenGLMatrix
//                    .translation(mmFTCFieldWidth, 0, mmTargetHeight)
//                    .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90));
//            backSpace.setLocation(backSpaceLocationOnField);
//
//            /**
//             * If using the rear (High Res) camera:
//             * We need to rotate the camera around it's long axis to bring the rear camera forward.
//             * This requires a negative 90 degree rotation on the Y axis
//             *
//             * Next, translate the camera lens to where it is on the robot.
//             * In this example, it is centered (left to right), but 110 mm forward of the middle of the robot, and 200 mm above ground level.
//             */
//
//            final int CAMERA_FORWARD_DISPLACEMENT = 110;   // eg: Camera is 110 mm in front of robot center
//            final int CAMERA_VERTICAL_DISPLACEMENT = 200;   // eg: Camera is 200 mm above ground
//            final int CAMERA_LEFT_DISPLACEMENT = 0;     // eg: Camera is ON the robot's center line
//
//            OpenGLMatrix phoneLocationOnRobot = OpenGLMatrix
//                    .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
//                    .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES,
//                            CAMERA_CHOICE == FRONT ? 90 : -90, 0, 0));
//
//            for (VuforiaTrackable trackable : allTrackables) {
//                ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
//            }
//
//            telemetry.addData(">", "Press Play to start tracking");
//            telemetry.update();
//
//            waitForStart();
//            robot.init(hardwareMap);
//
//            targetsRoverRuckus.activate();
//            while (opModeIsActive()) {
//                // check all the trackable target to see which one (if any) is visible.
//
//                telemetry.addData(">", "Tracking");
//                telemetry.update();
//
//
//                if (!targetFound) {
//                    telemetry.addLine("Target not Found");
//                    targetVisible = false;
//                    for (VuforiaTrackable trackable : allTrackables) {
//                        if (((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()) {
//                            telemetry.addData("Visible Target", trackable.getName());
//                            targetVisible = true;
//
//                            if (trackable.getName().matches("Blue-Rover")){
//                                BlueRoverV = true;
//                            }
//                            if (trackable.getName().matches("Red-Footprint")){
//                                RedFootprintV = true;
//                            }
//                            if (trackable.getName().matches("Front-Craters")){
//                                FrontCratersV = true;
//                            }
//                            if (trackable.getName().matches("Back-Space")){
//                                BackSpaceV = true;
//                            }
//                            // getUpdatedRobotLocation() will return null if no new information is available since
//                            // the last time that call was made, or if the trackable is not currently visible.
//                            OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener) trackable.getListener()).getUpdatedRobotLocation();
//                            if (robotLocationTransform != null) {
//                                lastLocation = robotLocationTransform;
//                            }
//                            break;
//                        }
//                    }
//                    // Provide feedback as to where the robot is located (if we know).
//                    if (targetVisible) {
//                        // express position (translation) of robot in mm.
//                        VectorF translation = lastLocation.getTranslation();
//                        telemetry.addData("Pos (mm)", "{X, Y, Z} = %.1f, %.1f, %.1f",
//                                translation.get(0), translation.get(1), translation.get(2));
//
//                        // express the rotation of the robot in degrees.
//                        Orientation rotation = Orientation.getOrientation(lastLocation, EXTRINSIC, XYZ, DEGREES);
//                        telemetry.addData("Rot (deg)", "{Roll, Pitch, Heading} = %.0f, %.0f, %.0f", rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle);
//
//                        FindTarget = true;
//                        robot.stopRobot();
//                        telemetry.addLine("Stop Target is visible");
//                        telemetry.update();
//                    } else {
//                        telemetry.addData("Visible Target", "none");
//                        //if (FindTarget == false) {
//                            robot.turnAroundUntilFound();
//                            telemetry.addLine("Turn around");
//                            telemetry.update();
//                        //}
//                    }
//                }
//                if (FindTarget){
//                    if (BlueRoverV || RedFootprintV ){
//                        robot.stopRobot();
//                        telemetry.addLine("Stop Blue or Red");
//                        telemetry.update();
//                        //robot.MoveFromCrator();
//                    }
//                    if (BackSpaceV|| FrontCratersV){
//                        robot.stopRobot();
//                        telemetry.addLine("Stop Back Space or Front Crater");
//                        telemetry.update();
//                        //robot.MoveFromCorner();
//                    }
//                }
//                telemetry.update();
//            }
//        }
//
//    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod () {
        telemetry.addLine("Tensorflow init");
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
        telemetry.update();
    }

    /*
     *  Method to perform a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */
    public void encoderDriveForwardorBackwards(double speed, double distanceMM, double timeoutS) {
        int newLeftFrontTarget;
        int newLeftRearTarget;
        int newRightFrontTarget;
        int newRightRearTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {
            //We use Tank Drive in all 4 wheels, so make sure we sent the correct signals to both Left and Right wheels
            // Determine new target position, and pass to motor controller
            newLeftFrontTarget = robot.motorLeftFront.getCurrentPosition() + (int)(distanceMM * COUNTS_PER_MM);
            newLeftRearTarget = robot.motorLeftRear.getCurrentPosition() + (int)(distanceMM * COUNTS_PER_MM);
            newRightFrontTarget = robot.motorRightFront.getCurrentPosition() + (int)(distanceMM * COUNTS_PER_MM);
            newRightRearTarget = robot.motorRightRear.getCurrentPosition() + (int)(distanceMM * COUNTS_PER_MM);

            robot.motorLeftFront.setTargetPosition(newLeftFrontTarget);
            robot.motorLeftRear.setTargetPosition(newLeftRearTarget);
            robot.motorRightFront.setTargetPosition(newRightFrontTarget);
            robot.motorRightRear.setTargetPosition(newRightRearTarget);

            // Turn On RUN_TO_POSITION
            robot.motorLeftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorLeftRear.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorRightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorRightRear.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            robot.motorLeftFront.setPower(Math.abs(speed));
            robot.motorRightFront.setPower(Math.abs(speed));

            robot.motorLeftRear.setPower(Math.abs(speed));
            robot.motorRightRear.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits, we should monitor ALL 4
            //but assume for noe only 2 or monitor 2
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH (actually all 4)  motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
//            while (opModeIsActive() &&
//                    (runtime.seconds() < timeoutS) &&
//                    (robot.motorLeftFront.isBusy() && robot.motorRightFront.isBusy()))

            //Only monitor the Back wheels
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.motorLeftRear.isBusy() &&  robot.motorRightRear.isBusy()))
//            while (opModeIsActive() &&
//                    (runtime.seconds() < timeoutS) &&
//                    (robot.motorLeftRear.isBusy() && robot.motorRightRear.isBusy()))
            {

                // Display it for the Debugging.
                //telemetry.addData("Path1",  "Running to Target LF,LR, RF, RR %7d :%7d", newLeftFrontTarget,  newRightFrontTarget);
                //telemetry.addData("Path2",  "Running at %7d :%7d",robot.motorLeftFront.getCurrentPosition(),robot.motorRightFront.getCurrentPosition());
                telemetry.addData("Path1",  "Running to Target LF,LR, RF, RR %7d :%7d :%7d :%7d", newLeftFrontTarget,  newLeftRearTarget, newRightFrontTarget,newRightRearTarget);
//                telemetry.addData("Path2",  "Running at %7d :%7d :%7d :%7d",
//                telemetry.addData("Path1",  "Running to Target LR, RR %7d :%7d", newLeftRearTarget, newRightRearTarget);
//                telemetry.addData("Path2",  "Running at %7d :%7d",
//                        robot.motorLeftRear.getCurrentPosition(),robot.motorRightRear.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion after Path is completed;
            robot.motorLeftFront.setPower(0);
            robot.motorLeftRear.setPower(0);

            robot.motorRightFront.setPower(0);
            robot.motorRightRear.setPower(0);
            // Turn off RUN_TO_POSITION
            robot.motorLeftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.motorRightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            robot.motorLeftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.motorRightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public void encoderTurn(double speed, double leftMMdistance, double rightMMdistance, double timeoutS){
        int newLeftFrontTarget;
        int newLeftRearTarget;
        int newRightFrontTarget;
        int newRightRearTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {
            //We use Tank Drive in all 4 wheels, so make sure we sent the correct signals to both Left and Right wheels
            // Determine new target position, and pass to motor controller
            newLeftFrontTarget = robot.motorLeftFront.getCurrentPosition() + (int)(leftMMdistance * COUNTS_PER_MM);
            newLeftRearTarget = robot.motorLeftRear.getCurrentPosition() + (int)(leftMMdistance * COUNTS_PER_MM);
            newRightFrontTarget = robot.motorRightFront.getCurrentPosition() + (int)(rightMMdistance * COUNTS_PER_MM);
            newRightRearTarget = robot.motorRightRear.getCurrentPosition() + (int)(rightMMdistance * COUNTS_PER_MM);

            robot.motorLeftFront.setTargetPosition(newLeftFrontTarget);
            robot.motorLeftRear.setTargetPosition(newLeftRearTarget);
            robot.motorRightFront.setTargetPosition(newRightFrontTarget);
            robot.motorRightRear.setTargetPosition(newRightRearTarget);

            // Turn On RUN_TO_POSITION
            robot.motorLeftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorLeftRear.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorRightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.motorRightRear.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            robot.motorLeftFront.setPower(Math.abs(speed));
            robot.motorRightFront.setPower(Math.abs(speed));

            robot.motorLeftRear.setPower(Math.abs(speed));
            robot.motorRightRear.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            //Only monitor the Back wheels
            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.motorLeftRear.isBusy() &&  robot.motorRightRear.isBusy()))
            {

                // Display it for the Debugging.
                telemetry.addData("Path1",  "Running to Target LF,LR, RF, RR %7d :%7d :%7d :%7d", newLeftFrontTarget,  newLeftRearTarget, newRightFrontTarget,newRightRearTarget);
                telemetry.update();
            }

            telemetry.addData("Power Reset","Power set to 0");
            telemetry.update();
            // Stop all motion after Path is completed;
            robot.motorLeftFront.setPower(0);
            robot.motorLeftRear.setPower(0);

            robot.motorRightFront.setPower(0);
            robot.motorRightRear.setPower(0);
            // Turn off RUN_TO_POSITION
            robot.motorLeftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.motorRightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            robot.motorLeftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.motorRightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    public void dropBeacon(double timeoutS){

        telemetry.addData("Beacon Drop", "Initialized");
        telemetry.update();
        runtime.reset();
        //dropBeaconServo.setPosition(0);
        telemetry.addData("Reset the Beacon",robot.dropBeaconServo.getPosition());

        robot.dropBeaconServo.setDirection(Servo.Direction.REVERSE);
        robot.dropBeaconServo.setPosition(0.5);

        robot.dropBeaconServo.setPosition(0.90);
        while (opModeIsActive()) {
            //WE don't have servo feedback
            telemetry.addData("Drop the Beacon and wait",robot.dropBeaconServo.getPosition());
            sleep(1000);
            telemetry.update();
            //if(robot.dropBeaconServo.getPosition()>0.89)
                break;

        }

        robot.dropBeaconServo.setPosition(0.5);
        telemetry.addData("Drop the Beacon",robot.dropBeaconServo.getPosition());

        telemetry.update();

    }

    //Use the Rev4wheel Telop to get the Values for various positions
    public void encoderMoveLift(int position, double speed,double timeoutS)
    {
        int newLiftget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newLiftget = robot.motorLift.getCurrentPosition() + position;//+ (int)(distanceMM * COUNTS_PER_MM);


            robot.motorLift.setTargetPosition(newLiftget);
            // Turn On RUN_TO_POSITION
            robot.motorLift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            // reset the timeout time and start motion.
            runtime.reset();
            robot.motorLift.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits, we should monitor ALL 4
            //but assume for noe only 2 or monitor 2
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH (actually all 4)  motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.

            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.motorLift.isBusy() ))
            {

                // Display it for the Debugging.
                 telemetry.addData("Lift Path",  "Running to Target :%7d", newLiftget);
                telemetry.update();
            }

            // Stop all motion after Path is completed;
            robot.motorLift.setPower(0);
            // Turn off RUN_TO_POSITION
            robot.motorLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        }
    }

    //Use the Rev4wheel Telop to get the Values for various positions
    public void encoderExtender(int position,double speed, double timeoutS)
    {
        int newExtendget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newExtendget = robot.motorExtend.getCurrentPosition() + position ;//+ (int)(distanceMM * COUNTS_PER_MM);


            robot.motorExtend.setTargetPosition(newExtendget);
            // Turn On RUN_TO_POSITION
            robot.motorExtend.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            // reset the timeout time and start motion.
            runtime.reset();
            robot.motorExtend.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits, we should monitor ALL 4
            //but assume for noe only 2 or monitor 2
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH (actually all 4)  motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.

            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.motorExtend.isBusy() ))
            {

                // Display it for the Debugging.
                telemetry.addData("Extender Path",  "Running to Target :%7d", newExtendget);
                telemetry.update();
            }

            // Stop all motion after Path is completed;
            robot.motorExtend.setPower(0);
            // Turn off RUN_TO_POSITION
            robot.motorExtend.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        }
    }

    public void encoderStrafe(double speed, double pair1, double pair2, double timeoutS)
    {
        int newLeftFrontTarget;
        int newLeftRearTarget;
        int newRightFrontTarget;
        int newRightRearTarget;

        newLeftFrontTarget = robot.motorLeftFront.getCurrentPosition() + (int)(pair1 * COUNTS_PER_MM);
        newLeftRearTarget = robot.motorLeftRear.getCurrentPosition() + (int)(pair2 * COUNTS_PER_MM);
        newRightFrontTarget = robot.motorRightFront.getCurrentPosition() + (int)(pair2 * COUNTS_PER_MM);
        newRightRearTarget = robot.motorRightRear.getCurrentPosition() + (int)(pair1 * COUNTS_PER_MM);

        robot.motorLeftFront.setTargetPosition(newLeftFrontTarget);
        robot.motorLeftRear.setTargetPosition(newLeftRearTarget);
        robot.motorRightFront.setTargetPosition(newRightFrontTarget);
        robot.motorRightRear.setTargetPosition(newRightRearTarget);

        // Turn On RUN_TO_POSITION
        robot.motorLeftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.motorLeftRear.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.motorRightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.motorRightRear.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // reset the timeout time and start motion.
        runtime.reset();
        robot.motorLeftFront.setPower(Math.abs(speed));
        robot.motorRightFront.setPower(Math.abs(speed));

        robot.motorLeftRear.setPower(Math.abs(speed));
        robot.motorRightRear.setPower(Math.abs(speed));

        // keep looping while we are still active, and there is time left, and both motors are running.
        //Only monitor the Back wheels
        while (opModeIsActive() &&
                (runtime.seconds() < timeoutS) &&
                (robot.motorLeftRear.isBusy() &&  robot.motorRightRear.isBusy()))
        {

            // Display it for the Debugging.
            telemetry.addData("Path1",  "Running to Target LF,LR, RF, RR %7d :%7d :%7d :%7d", newLeftFrontTarget,  newLeftRearTarget, newRightFrontTarget,newRightRearTarget);
            telemetry.update();
        }

        // Stop all motion after Path is completed;
        robot.motorLeftFront.setPower(0);
        robot.motorLeftRear.setPower(0);

        robot.motorRightFront.setPower(0);
        robot.motorRightRear.setPower(0);
        // Turn off RUN_TO_POSITION
        robot.motorLeftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorRightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        robot.motorLeftRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.motorRightRear.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

}

