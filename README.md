# REPHOTO
### UPDATE 4-16
UI
ORB Parallel
### UPDATE 4-23
|State				|BT-TakePhoto		 |BT-Open	   |BT-Ok		 |BT-Close     |Text-Info|Text-Save|Text-Image|
|:-----------------:|:------------------:|:-----------:|:-----------:|:-----------:|:-------:|:-------:|:--------:|
|1:=Init			|Visible & Blue	==> 2|Visible ==> 3|InVisible	 |InVisible	   |InVisible|InVisible|InVisible |
|2:=Reference Image	|InVisible			 |InVisible    |Visible	==> 3|Visible ==> 1|InVisible|Visible  |InVisible |
|3:=Ready			|Visible & Blue	==> 4|InVisible    |InVisible	 |Visible ==> 1|InVisible|InVisible|InVisible |
|4:=RephotoING		|Visible & RED	==> 5|InVisible    |InVisible	 |InVisible	   |Visible	 |InVisible|InVisible |
|5:=RephotoStop		|InVisible			 |InVisible    |Visible	==> 6|Visible ==> 3|INVisible|InVisible|InVisible |
|6:=Switch Image	|InVisible			 |InVisible    |Visible		 |Visible ==> 3|InVisible|InVisible|Visible	  |
