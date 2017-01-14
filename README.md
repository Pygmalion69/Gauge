# Gauge
A Gauge View for Android

![Android Gauge](device-screenshot-2.png "Android Gauge")

###Example

```xml
<de.nitri.gauge.Gauge
            android:id="@+id/gauge2"
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

###XML attributes
####totalNicks
Total number of nicks on a full 360 degree scale.
####valuePerNick
Float value (interval) per nick.
####majorNickInterval
Integer interval (number of nicks) between enlarged nicks.
####minValue
Float minimum value.
####maxValue
Float maximum value.
####intScale
Boolean for an integer scale (defaults to true).
####initialValue
Float initial value.
####labelTextSize
Float text size for the number labels (defaults to a calculated value).
####faceColor
Integer face color.
####scaleColor
Integer scale color.
####needleColor
Integer needle color.
####needleShadow
Boolean to apply a shadow effect to the needele (defaults to true).

###Public methods
####void setValue(float value)
Set gauge to value.
####void moveToValue(float value)
Animate gauge to value.

