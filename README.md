# Step-1
Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
# Step-2
Add the dependency
```
    dependencies {
    	        implementation 'com.github.Seasonallan:Ipfs-Android:2.0'
    	}
```
# Step-03
need permission
<uses-permission android:name="android.permission.INTERNET" />

1、 start ipfs：
```
       IPFS ipfs = new IPFS(activity.getApplicationContext());
       ipfs.start();
```
2、 get your cid
```
       ArrayList<JSONObject> jsonList = ipfs.newRequest("id").sendToJSONList();
       String cid = jsonList.get(0).getString("ID");
```
3、 add file 
```
       ArrayList<JSONObject> jsonList = ipfs.newRequest("add")
                           .withHeader("Content-Type", "multipart/form-data; boundary=------------------------5f505897199c8c52")
                           .withBody(body)
                           .sendToJSONList();
       String cid = jsonList.get(0).getString("Hash");
```
3、 cat file 
```
       byte[] fetchedData = ipfs.newRequest("cat")
                       .withArgument(cid)
                       .send();
       //decode byte[] to image
       Bitmap bitmap = BitmapFactory.decodeByteArray(fetchedData, 0, fetchedData.length);
       //decode byte[] to text 
       String text = new String(fetchedData, StandardCharsets.UTF_8);
```

 

Then repo was quickly moved from IPFS Shipyard and made open-source. It currently contains the following components:   
   
A golang part which exposes the essential functions for the init of the node and the functions related to the RequestBuilder   
Two Swift and Java packages that facilitate node management and send requests from these native languages   
Two demo applications for Android and iOS using gomobile-ipfs that simply download a random XKCD over IPFS   
 