package android.test.exifapplication;

import android.app.Activity;
import android.compact.utils.FileCompactUtil;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;

import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.RationalNumber;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import tools.android.filedownloader.DownloadAdatper;
import tools.android.filedownloader.FileDownloadManager;

import static org.apache.sanselan.formats.tiff.constants.TiffDirectoryConstants.EXIF_DIRECTORY_IFD0;
import static org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants.FIELD_TYPE_ASCII;
import static org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants.FIELD_TYPE_DESCRIPTION_ASCII;

public class MainActivity extends Activity {

    String exif_jpg = "https://gist.github.com/liuchonghui/277cd9fac31b8cff8c9ccbc3600b55fd/raw/cf43f879be15405ee9b7956b44f74ab611f6890a/exif.jpg";
    String exif_path = "";
    String to_path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        exif_jpg = "https://gist.github.com/liuchonghui/277cd9fac31b8cff8c9ccbc3600b55fd/raw/e87dbc9a7881da0a90bac938ee5cce7d44e1815a/7C079ECDDA8D1FB8A087CADF8D010651.jpg";
        FileDownloadManager.get().downloadFile(getApplicationContext(),
                "exif", "jpg", "", exif_jpg, new DownloadAdatper() {
                    @Override
                    public void onDownloadClear(boolean success, String url, String path) {
                        if (success) {
                            Log.d("PPP", "onDownloadClear|" + path);
                            Toast.makeText(getApplicationContext(), "exif.jpg download!", Toast.LENGTH_LONG).show();
                            exif_path = path;
                        }
                    }
                });
        Button btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OutputStream os = null;
                Log.d("Exif", "path|from|" + exif_path);
                File from = new File(exif_path);
                File to = new File(FileCompactUtil.getTempDirPath(getApplicationContext()), "to.jpg");
                if (!to.exists()) {
                    try {
                        to.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                to_path = to.getAbsolutePath();
                Log.d("Exif", "path|to|" + to_path);

                Log.d("Exif", "md5|from|" + FileCompactUtil.getMD5(from) + "|size|" + from.length());
                try {
                    changeExifMetadata(from, to);
                    File newTo = new File(to_path);
                    Log.d("Exif", "md5|to|" + FileCompactUtil.getMD5(newTo) + "|size|" + newTo.length());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Exif", "changeExifMetadata|e|" + e.getMessage());
                }
            }
        });
        Button btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ImageInfo info = Sanselan.getImageInfo(new File(exif_path));
                    Log.d("Exif", "imageinfo|from|" + info.toString());
                    IImageMetadata metadata = Sanselan.getMetadata(new File(exif_path));
                    Log.d("Exif", "metadata|from|" + metadata.toString());
                    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                    if (jpegMetadata != null) {
                        TiffImageMetadata exif = jpegMetadata.getExif();
                        Log.d("Exif", "exif|from|" + exif.toString());
                    }
                } catch (ImageReadException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    ImageInfo info = Sanselan.getImageInfo(new File(to_path));
                    Log.d("Exif", "imageinfo|to|" + info.toString());
                    IImageMetadata metadata = Sanselan.getMetadata(new File(exif_path));
                    Log.d("Exif", "metadata|to|" + metadata.toString());
                    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                    if (jpegMetadata != null) {
                        TiffImageMetadata exif = jpegMetadata.getExif();
                        Log.d("Exif", "exif|to|" + exif.toString());
                    }
                } catch (ImageReadException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    read(exif_path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    read(to_path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void changeExifMetadata(final File jpegImageFile, final File dst)
            throws IOException, ImageReadException, ImageWriteException {
        OutputStream os = null;
        try {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                    Log.d("PPP", "outputSet.byteOrder|" + outputSet.byteOrder);
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                // Example of how to add a field/tag to the output set.
                //
                // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate
                // tags. See above.
                //
                // Certain fields/tags are expected in certain Exif directories;
                // Others can occur in more than one directory (and often have a
                // different meaning in different directories).
                //
                // TagInfo constants often contain a description of what
                // directories are associated with a given tag.
                //
                // see
                // org.apache.commons.sanselan.formats.tiff.constants.AllTagConstants
                //
                TiffOutputDirectory exifDirectory = outputSet
                        .getOrCreateExifDirectory();
                exifDirectory = outputSet.getOrCreateRootDirectory();
                // make sure to remove old value if present (this method will
                // not fail if the tag does not exist).
//                exifDirectory
//                        .removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
//                exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE,
//                        RationalNumber.factoryMethod(3, 10));

//                TagInfo tagInfo = ExifTagConstants.EXIF_TAG_IMAGE_DESCRIPTION;
//                exifDirectory.removeField(tagInfo);
//                exifDirectory.add(new TiffOutputField(tagInfo, tagInfo.dataTypes[0], 1, "hello".getBytes()));

                TagInfo EXIF_TAG_IMAGE_DESCRIPTION = new TagInfo(
                        "Image Description", 0x010e, FIELD_TYPE_DESCRIPTION_ASCII, 1,
                        EXIF_DIRECTORY_IFD0);

                TagInfo EXIF_TAG_SOFTWARE = new TagInfo("Software",
                        0x0131, FIELD_TYPE_DESCRIPTION_ASCII, 1, EXIF_DIRECTORY_IFD0);

                TiffOutputField descriptionRefField = TiffOutputField.create(
                        EXIF_TAG_SOFTWARE,
                        outputSet.byteOrder,
                        "www.mi.com");
//                exifDirectory.removeField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);
                exifDirectory.removeField(TiffConstants.EXIF_TAG_SOFTWARE);

                descriptionRefField = new TiffOutputField(EXIF_TAG_SOFTWARE.tag, EXIF_TAG_SOFTWARE, FIELD_TYPE_ASCII,
                        "www.mi.com".length(), "www.mi.com".getBytes());

                exifDirectory.add(descriptionRefField);


//                TiffOutputField longitudeRefField = TiffOutputField.create(
//                        TiffConstants.GPS_TAG_GPS_LONGITUDE_REF,
//                        TiffConstants.DEFAULT_TIFF_BYTE_ORDER,
//                        "W");
//                exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
//                exifDirectory.add(longitudeRefField);
            }


//            {
//                // Example of how to add/update GPS info to output set.
//
//                // New York City
//                double longitude = -74.0; // 74 degrees W (in Degrees East)
//                double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
//                // North)
//
////                outputSet.setGPSInDegrees(longitude, latitude);
//
//                TiffOutputDirectory gpsDirectory = outputSet.getOrCreateGPSDirectory();
//
//                String longitudeRef = longitude < 0 ? "W" : "E";
//                longitude = Math.abs(longitude);
//                String latitudeRef = latitude < 0 ? "S" : "N";
//                latitude = Math.abs(latitude);
//
//                {
//                    TiffOutputField longitudeRefField = TiffOutputField.create(
//                            TiffConstants.GPS_TAG_GPS_LONGITUDE_REF, outputSet.byteOrder,
//                            longitudeRef);
//                    gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
//                    gpsDirectory.add(longitudeRefField);
//                }
//
//                {
//                    TiffOutputField latitudeRefField = TiffOutputField.create(
//                            TiffConstants.GPS_TAG_GPS_LATITUDE_REF, outputSet.byteOrder,
//                            latitudeRef);
//                    gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
//                    gpsDirectory.add(latitudeRefField);
//                }
//
//                {
//                    double value = longitude;
//                    double longitudeDegrees = (long) value;
//                    value %= 1;
//                    value *= 60.0;
//                    double longitudeMinutes = (long) value;
//                    value %= 1;
//                    value *= 60.0;
//                    double longitudeSeconds = value;
//                    Double values[] = {
//                            new Double(longitudeDegrees), new Double(longitudeMinutes),
//                            new Double(longitudeSeconds),
//                    };
//
//                    TiffOutputField longitudeField = TiffOutputField.create(
//                            TiffConstants.GPS_TAG_GPS_LONGITUDE, outputSet.byteOrder, values);
//                    gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
//                    gpsDirectory.add(longitudeField);
//                }
//
//                {
//                    double value = latitude;
//                    double latitudeDegrees = (long) value;
//                    value %= 1;
//                    value *= 60.0;
//                    double latitudeMinutes = (long) value;
//                    value %= 1;
//                    value *= 60.0;
//                    double latitudeSeconds = value;
//                    Double values[] = {
//                            new Double(latitudeDegrees), new Double(latitudeMinutes),
//                            new Double(latitudeSeconds),
//                    };
//
//                    TiffOutputField latitudeField = TiffOutputField.create(
//                            TiffConstants.GPS_TAG_GPS_LATITUDE, outputSet.byteOrder, values);
//                    gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
//                    gpsDirectory.add(latitudeField);
//                }
//            }

            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);

            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);

            os.close();
            os = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final Exception e) {
                    Log.d("Exif", "changeExifMetadata|Exception|" + e.getMessage());
                }
            }
        }
    }

    public void read(String path) throws Exception {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
        String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        String make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
        String model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        String flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
        String imageLength = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
        String imageWidth = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        String exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        String aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
        String isoSpeedRatings = exifInterface.getAttribute(ExifInterface.TAG_ISO);
        String dateTimeDigitized = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
        String subSecTime = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME);
        String subSecTimeOrig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIG);
        String subSecTimeDig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIG);
        String altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        String altitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
        String gpsTimeStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
        String gpsDateStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
        String whiteBalance = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
        String focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        String processingMethod = exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
        String software = exifInterface.getAttribute(ExifInterface.TAG_SOFTWARE);
        String imageDescription = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        Log.e("TAG", "## READ ##" + path);
        Log.e("TAG", "## orientation=" + orientation);
        Log.e("TAG", "## dateTime=" + dateTime);
        Log.e("TAG", "## make=" + make);
        Log.e("TAG", "## model=" + model);
        Log.e("TAG", "## flash=" + flash);
        Log.e("TAG", "## imageLength=" + imageLength);
        Log.e("TAG", "## imageWidth=" + imageWidth);
        Log.e("TAG", "## latitude=" + latitude);
        Log.e("TAG", "## longitude=" + longitude);
        Log.e("TAG", "## latitudeRef=" + latitudeRef);
        Log.e("TAG", "## longitudeRef=" + longitudeRef);
        Log.e("TAG", "## exposureTime=" + exposureTime);
        Log.e("TAG", "## aperture=" + aperture);
        Log.e("TAG", "## isoSpeedRatings=" + isoSpeedRatings);
        Log.e("TAG", "## dateTimeDigitized=" + dateTimeDigitized);
        Log.e("TAG", "## subSecTime=" + subSecTime);
        Log.e("TAG", "## subSecTimeOrig=" + subSecTimeOrig);
        Log.e("TAG", "## subSecTimeDig=" + subSecTimeDig);
        Log.e("TAG", "## altitude=" + altitude);
        Log.e("TAG", "## altitudeRef=" + altitudeRef);
        Log.e("TAG", "## gpsTimeStamp=" + gpsTimeStamp);
        Log.e("TAG", "## gpsDateStamp=" + gpsDateStamp);
        Log.e("TAG", "## whiteBalance=" + whiteBalance);
        Log.e("TAG", "## focalLength=" + focalLength);
        Log.e("TAG", "## processingMethod=" + processingMethod);
        Log.e("TAG", "## software=" + software);
        Log.e("TAG", "## imageDescription=" + imageDescription);
    }
}
