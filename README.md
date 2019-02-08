# CustomCompactCalendar
Android Library - Customizable compact calendar view

Forked from https://github.com/SundeepK/CompactCalendarView

## Gradle Dependency

Add it in your root build.gradle at the end of repositories:
```jshelllanguage

allprojects {
		repositories {
		    
			maven { url 'https://jitpack.io' }
		}
	}

```
Add the dependency
```jshelllanguage
dependencies {
     implementation 'com.github.saran2somu:CustomCompactCalendar:1.0.0'
	     }
```

Integration and usage 

* Refer : https://github.com/SundeepK/CompactCalendarView

### Attribute and methods

#### Text Size
*  app:compactCalendarDateTextSize="@dimen/_10sdp"
*  app:compactCalendarWeekTextSize="@dimen/_11sdp"   

#### Text Color
* app:compactCalendarWeekDaysTextColor
* app:compactCalendarWeekEndDaysTextColor
* app:compactCalendarTextColor
* app:compactCalendarWeekNamesBackgroundColor

#### Min and Max Year 
* app:compactCalendarMinYear="2018"
* app:compactCalendarMaxYear="2050"

#### Typeface 
* setWeekTextTypeface(Typeface weekDayTypeFace)
* setDateTextTypeface(Typeface weekDayTypeFace)

#### Next and Previous Month swipe 
* showNextMonth
* showPreviousMonth








