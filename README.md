# Gauge
A Gauge View for Android

[![Release](https://jitpack.io/v/Pygmalion69/Gauge.svg)](https://jitpack.io/#Pygmalion69/Gauge) [![Build Status](https://travis-ci.org/Pygmalion69/Gauge.svg?branch=master)](https://travis-ci.org/Pygmalion69/Gauge)

![Android Gauge](device-screenshot-2.png "Android Gauge")

See my [blog post](http://pygmalion.nitri.de/android-gauge-view-1039.html) for a brief introduction.

### Example

```xml
<de.nitri.gauge.Gauge
            android:id="@+id/gauge"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_marginTop="10dp"
            gauge:initialValue="22"
            gauge:maxValue="100"
            gauge:minValue="0"
            gauge:totalNicks="120"
            gauge:valuePerNick="1" />
```

### XML attributes
#### totalNicks
Total number of nicks on a full 360 degree scale. This should be a multiple of majorNickInterval.
#### valuePerNick
Float value (interval) per nick.
#### majorNickInterval
Integer interval (number of nicks) between enlarged nicks.

*Note that there is always a major nick on top, that is at 0 degrees, so
it is advisable that the sum of the min and max value be divisible by 2.*

#### minValue
Float minimum value.
#### maxValue
Float maximum value.
#### intScale
Boolean for an integer scale (defaults to true).
#### initialValue
Float initial value.
#### labelTextSize
Float text size for the number labels (defaults to a calculated value).
#### faceColor
Integer face color.
#### scaleColor
Integer scale color.
#### needleColor
Integer needle color.
#### needleShadow
Boolean to apply a shadow effect to the needle (defaults to true).
#### upperText
String to display on upper gauge face (e.g. a quantity).
#### lowerText
String to display on lower gauge face (e.g. a unit).
#### textSize
Float text size for upper and lower text.
#### upperTextSize
Float text size for upper text.
#### lowerTextSize
Float text size for lower text.

*Text size is in pixels at a screen width (max. canvas width/height) of 1080 and is scaled
accordingly at different resolutions. E.g. a value of 48 is unchanged at 1080 x 1920
and scaled down to 27 at 600 x 1024.*

### Public methods
#### void setValue(float value)
Set gauge to value.
#### void moveToValue(float value)
Animate gauge to value.
#### void setUpperText(String text)
Set string to display on upper gauge face.
#### void setLowerText(String text)
Set string to display on lower gauge face.
#### void setTextSize(float relative pixels)
Set a text size for the upper and lower text.
#### void setUpperTextSize(float relative pixels)
Set or override the text size for the upper text.
#### void setLowerTextSize(float relative pixels)
Set or override the text size for the lower text.
#### void setDeltaTimeInterval(int value)
Set the delta time between movement steps during needle animation (default: 5 ms).
#### void setNeedleStepFactor(float value)
Set the factor that determines the step size during needle animation (default: 3f).
The actual step size is calulated as follows: step_size = step_factor * scale_value_per_degree.
#### void setMinValue(float value)
Set the minimum scale value.
#### void setMaxValue(float value)
Set the maximum scale value.
####  void setTotalNicks(int nicks)
Set the total amount of nicks on a full 360 degree scale. Should be a multiple of majorNickInterval.
#### void setValuePerNick(float value)
Set the value (interval) per nick.
#### void setMajorNickInterval(int interval)
Set the interval (number of nicks) between enlarged nicks.

*Text size is in pixels at a screen width (max. canvas width/height) of 1080 and is scaled
accordingly at different resolutions. E.g. a value of 48 is unchanged at 1080 x 1920
and scaled down to 27 at 600 x 1024.*

### Javadoc
[http://pygmalion.nitri.org/javadoc/gauge/](http://pygmalion.nitri.org/javadoc/gauge/)


### Gradle

Add the JitPack repository in your root build.gradle at the end of repositories:

```
repositories {
    maven {
        url 'https://www.jitpack.io'
    }
}
```

Add the dependency:

```
dependencies {
    implementation 'com.github.Pygmalion69:Gauge:1.5.2'
}
```

### App implementing this view

[ErsaAndroid](https://github.com/Pygmalion69/ErsaAndroid)
