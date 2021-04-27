# ConnectivityCheck
[![](https://jitpack.io/v/Ashusnu/ConnectivityCheck.svg)](https://jitpack.io/#Ashusnu/ConnectivityCheck)


<h2>How to use:</h2>

Step 1) Add it in your root build.gradle at the end of repositories:

```

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
}
  
```

Step 2) Add the dependency

```

implementation 'com.github.Ashusnu:ConnectivityCheck:$version'

```

Step 3) Attach connectivity check listener, This can be done in two ways as below:

a)  Initialize listner in onCreate() method.

```

 ConnectivityCheck.checkConnectionState(connectionInfo -> {
           // do something with connectionInfo
 });
```

b) Implementing ConnectivityCheck.connectionStateListener.

```

public class MainActivity extends AppCompatActivity implements ConnectivityCheck.connectionStateListener {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
   }
        
  @Override
    public void connectionState(ConnectionInfo connectionInfo) {
        // do something with connectionInfo
    }      
      
}
```

Step 3) Finally start and stop connection check

To start connection check call startCheck() inside onResume() block

```

 @Override
    protected void onResume() {
        super.onResume();
        ConnectivityCheck.startCheck();
    }
```

To stop connection check call stopCheck()inside onPause() block

```

@Override
    protected void onPause() {
        super.onPause();
        ConnectivityCheck.stopCheck();
    }


```

<h2>That's it!<h2>
