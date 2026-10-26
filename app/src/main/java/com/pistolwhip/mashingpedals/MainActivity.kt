package com.pistolwhip.mashingpedals
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.UUID
import kotlin.math.max

class MainActivity:Activity(){
 private val p by lazy{getSharedPreferences("mp",MODE_PRIVATE)}
 private lateinit var db:TripDatabase
 private var module="GPS";private var dark=true;private var compact=false;private var accent=Color.rgb(0,230,180)
 private val v=mutableMapOf<String,Boolean>()
 private var speed:Double?=null;private var distance=0.0;private var secs=0L;private var maxSpeed=0.0;private var speedSum=0.0;private var speedN=0L
 private var heart:Int?=null;private var maxHeart:Int?=null;private var heartSum=0L;private var heartN=0L
 private var cadence:Int?=null;private var maxCadence:Int?=null;private var cadenceSum=0L;private var cadenceN=0L
 private var gd=0.0;private var gs=0L;private var gmax=0.0;private var gsum=0.0;private var gn=0L;private var ghmax:Int?=null;private var ghs=0L;private var ghn=0L
 private var prev:Location?=null;private var tickAt=0L;private var gatt:BluetoothGatt?=null
 private val h=Handler(Looper.getMainLooper())
 private val svc=UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");private val hrid=UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");private val cccd=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
 private val keys=listOf("section.speed","section.trip","section.heart","section.cadence","speed.current","speed.max","speed.avg","trip.time","trip.distance","heart.current","heart.max","heart.avg","cadence.current","cadence.max","cadence.avg")
 private val clock=object:Runnable{override fun run(){val now=System.currentTimeMillis();if((speed?:0.0)>0&&tickAt>0){val d=((now-tickAt)/1000).coerceIn(0,3);secs+=d;gs+=d};tickAt=now;saveData();render();h.postDelayed(this,1000)}}
 override fun onCreate(b:Bundle?){super.onCreate(b);db=TripDatabase(this);dark=p.getBoolean("dark",true);compact=p.getBoolean("compact",false);accent=p.getInt("accent",Color.rgb(0,230,180));keys.forEach{v[it]=p.getBoolean(it,true)};loadData();render();h.post(clock);if(!p.getBoolean("warnDismiss",false))h.postDelayed({warning()},350);requestGps()}
 override fun onDestroy(){h.removeCallbacks(clock);try{gatt?.close()}catch(_:Exception){};super.onDestroy()}
 private fun loadData(){distance=p.getFloat("d",0f).toDouble();secs=p.getLong("s",0);maxSpeed=p.getFloat("ms",0f).toDouble();speedSum=p.getFloat("ss",0f).toDouble();speedN=p.getLong("sn",0);heart=p.getInt("hr",-1).takeIf{it>=0};maxHeart=p.getInt("hm",-1).takeIf{it>=0};heartSum=p.getLong("hs",0);heartN=p.getLong("hn",0);cadence=p.getInt("ca",-1).takeIf{it>=0};maxCadence=p.getInt("cm",-1).takeIf{it>=0};cadenceSum=p.getLong("cs",0);cadenceN=p.getLong("cn",0);gd=p.getFloat("gd",0f).toDouble();gs=p.getLong("gs",0);gmax=p.getFloat("gm",0f).toDouble();gsum=p.getFloat("gss",0f).toDouble();gn=p.getLong("gn",0);ghmax=p.getInt("ghm",-1).takeIf{it>=0};ghs=p.getLong("ghs",0);ghn=p.getLong("ghn",0)}
 private fun saveData(){p.edit().putFloat("d",distance.toFloat()).putLong("s",secs).putFloat("ms",maxSpeed.toFloat()).putFloat("ss",speedSum.toFloat()).putLong("sn",speedN).putInt("hr",heart?:-1).putInt("hm",maxHeart?:-1).putLong("hs",heartSum).putLong("hn",heartN).putInt("ca",cadence?:-1).putInt("cm",maxCadence?:-1).putLong("cs",cadenceSum).putLong("cn",cadenceN).putFloat("gd",gd.toFloat()).putLong("gs",gs).putFloat("gm",gmax.toFloat()).putFloat("gss",gsum.toFloat()).putLong("gn",gn).putInt("ghm",ghmax?:-1).putLong("ghs",ghs).putLong("ghn",ghn).apply()}
 private fun requestGps(){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),20) else gps()}
 @SuppressLint("MissingPermission") private fun gps(){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return;try{val lm=getSystemService(Context.LOCATION_SERVICE) as LocationManager;lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1f,object:LocationListener{override fun onLocationChanged(l:Location){val old=prev;if(old!=null){val km=old.distanceTo(l).toDouble()/1000;val k=if(l.hasSpeed())l.speed*3.6 else km*3600;if(k in 0.0..150.0){speed=k;if(k>0){distance+=km;gd+=km;maxSpeed=max(maxSpeed,k);gmax=max(gmax,k);speedSum+=k;speedN++;gsum+=k;gn++};render()}};prev=l};override fun onProviderDisabled(provider:String){speed=null;render()};override fun onProviderEnabled(provider:String){};@Deprecated("Deprecated in Java") override fun onStatusChanged(provider:String?,status:Int,extras:Bundle?) {}})}catch(_:Exception){speed=null}}
 private fun warning(){val content=LinearLayout(this).apply{orientation=1};content.addView(txt("Heart rate: connect Gear S3 if it exposes the standard Bluetooth LE Heart Rate Service.\n\nCadence: no source configured. It remains unavailable, not zero.",13,if(dark)Color.LTGRAY else Color.DKGRAY,false));ridePanel("Data source warning",content,listOf("Source settings" to {p.edit().putBoolean("warnDismiss",true).apply();settings()},"Dismiss" to {p.edit().putBoolean("warnDismiss",true).apply()}))}
 private fun render(){val bg=if(dark)Color.BLACK else Color.rgb(246,248,250);val fg=if(dark)Color.WHITE else Color.DKGRAY;val muted=if(dark)Color.LTGRAY else Color.GRAY;window.statusBarColor=bg;window.navigationBarColor=bg
 val root=LinearLayout(this).apply{orientation=0;setPadding(dp(10),dp(8),dp(10),dp(8));setBackgroundColor(bg)};val left=LinearLayout(this).apply{orientation=1;setPadding(dp(4),0,dp(8),0)};val right=LinearLayout(this).apply{orientation=1;setPadding(dp(7),dp(7),dp(7),dp(7));setBackgroundColor(bg);background=null;foreground=null}
 root.addView(left,LinearLayout.LayoutParams(0,-1,2f));root.addView(right,LinearLayout.LayoutParams(0,-1,1f).apply{leftMargin=dp(4)});left.addView(txt("Mashing Pedals",22,fg,true));left.addView(txt(module+" module",12,muted,false).apply{setPadding(0,dp(3),0,dp(8))})
 val body=LinearLayout(this).apply{orientation=1};val sc=ScrollView(this);sc.addView(body);left.addView(sc,LinearLayout.LayoutParams(-1,0,1f))
 when(module){"GPS"->{body.addView(txt("GPS and navigation",20,fg,true));body.addView(txt("Location and route features are planned for a later phase.",16,muted,false));btn(body,"Test command"){commandDialog()}}
 "Spotify"->body.addView(txt("Spotify integration is planned for a later phase.",16,muted,false))
 "pulsar"->body.addView(txt("Pulsar media controls are planned for a later phase.",16,muted,false))
 "YouTube"->body.addView(txt("YouTube module integration is planned for a later phase.",16,muted,false))
 "Samsung Health"->body.addView(txt("Heart rate: "+(heart?.toString()?.plus(" bpm")?:"Unavailable")+"\nCadence: unavailable\nGear S3 compatibility depends on standard BLE service support.",16,muted,false))
 "Settings"->{body.addView(txt("Settings",20,fg,true));btn(body,"Ride computer settings"){settings()};btn(body,"Scan Bluetooth heart-rate device"){scan()};btn(body,"Saved trip history"){history()}}}
 val nav=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER}
 listOf(Triple("GPS","⌖",Color.CYAN),Triple("Spotify","♫",Color.GREEN),Triple("pulsar","▶",Color.MAGENTA),Triple("YouTube","▷",Color.RED),Triple("Samsung Health","♥",Color.RED),Triple("Settings","⚙",Color.YELLOW)).forEach{itx->val q=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER;setBackgroundColor(Color.TRANSPARENT);foreground=null;setOnClickListener{module=itx.first;render()};contentDescription="Open "+itx.first+" module"}
 val glyph=TextView(this).apply{text=itx.second;textSize=20f;gravity=Gravity.CENTER;setTextColor(itx.third);setBackgroundColor(Color.TRANSPARENT);foreground=null}
 val label=TextView(this).apply{text=itx.first;textSize=8f;gravity=Gravity.CENTER;setTextColor(if(module==itx.first)accent else muted);maxLines=1;setBackgroundColor(Color.TRANSPARENT);foreground=null}
 q.addView(glyph);q.addView(label);nav.addView(q,LinearLayout.LayoutParams(0,dp(44),1f))}
 left.addView(nav,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
 val tools=LinearLayout(this).apply{orientation=0;setBackgroundColor(Color.TRANSPARENT);background=null;foreground=null};icon(tools,"↻","Trip actions",accent){tripActions()};icon(tools,"▥","Global statistics",Color.CYAN){globals()};icon(tools,"⚙","Settings",Color.YELLOW){settings()};right.addView(tools,LinearLayout.LayoutParams(-1,dp(38)));right.addView(txt("RIDE COMPUTER",11,fg,true))
 val all=listOf(Triple("SPEED","section.speed",listOf(Triple("Current","speed.current",speed?.let{String.format("%.1f",it)+" km/h"}?:"—"),Triple("Max","speed.max",if(speedN>0)String.format("%.1f km/h",maxSpeed)else"—"),Triple("Avg","speed.avg",if(speedN>0)String.format("%.1f km/h",speedSum/speedN)else"—"))),Triple("TRIP","section.trip",listOf(Triple("Time","trip.time",time(secs)),Triple("Distance","trip.distance",String.format("%.2f km",distance)))),Triple("HEART RATE","section.heart",listOf(Triple("Current","heart.current",heart?.let{"$it bpm"}?:"—"),Triple("Max","heart.max",maxHeart?.let{"$it bpm"}?:"—"),Triple("Avg","heart.avg",if(heartN>0)(heartSum/heartN).toString()+" bpm"else"—"))),Triple("CADENCE","section.cadence",listOf(Triple("Current","cadence.current",cadence?.let{"$it rpm"}?:"—"),Triple("Max","cadence.max",maxCadence?.let{"$it rpm"}?:"—"),Triple("Avg","cadence.avg",if(cadenceN>0)(cadenceSum/cadenceN).toString()+" rpm"else"—"))))
 val rows=all.map{it.first to it.third.filter{m->v[m.second]==true}}.filter{pair->v[when(pair.first){"SPEED"->"section.speed";"TRIP"->"section.trip";"HEART RATE"->"section.heart";else->"section.cadence"}]==true&&pair.second.isNotEmpty()}
 if(rows.isEmpty())right.addView(txt("No metrics visible. Open settings.",12,muted,false))
 rows.forEachIndexed{idx,pair->val block=LinearLayout(this).apply{orientation=1;setPadding(0,dp(3),0,dp(3));setBackgroundColor(Color.TRANSPARENT);background=null;foreground=null};block.addView(txt(pair.first,10,accent,true));val line=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER};pair.second.forEach{m->val cell=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER};cell.addView(txt(m.first.uppercase(),8,muted,true));cell.addView(txt(m.third,if(compact)9 else 11,fg,true).apply{gravity=Gravity.CENTER;maxLines=2});line.addView(cell,LinearLayout.LayoutParams(0,-1,1f))};block.addView(line,LinearLayout.LayoutParams(-1,0,1f));right.addView(block,LinearLayout.LayoutParams(-1,0,1f).apply{if(idx>0)topMargin=dp(4)})}
 setContentView(root)}
 private fun settings(){
  val draft=v.toMutableMap()
  val root=LinearLayout(this).apply{orientation=1}
  val tabs=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER}
  val pages=mutableListOf<LinearLayout>()
  fun page():LinearLayout=LinearLayout(this).apply{orientation=1;setPadding(dp(8),dp(6),dp(8),dp(6))}
  val measure=page();val connect=page();val style=page()
  pages.add(measure);pages.add(connect);pages.add(style)
  fun toggle(host:LinearLayout,k:String,label:String){val q=CheckBox(this).apply{text=label;isChecked=draft[k]==true};q.setOnCheckedChangeListener{_,checked->draft[k]=checked};host.addView(q)}
  toggle(measure,"section.speed","Speed section");toggle(measure,"speed.current","Current speed");toggle(measure,"speed.max","Max speed");toggle(measure,"speed.avg","Average speed")
  toggle(measure,"section.trip","Trip section");toggle(measure,"trip.time","Trip time");toggle(measure,"trip.distance","Trip distance")
  toggle(measure,"section.heart","Heart-rate section");toggle(measure,"heart.current","Current heart rate");toggle(measure,"heart.max","Max heart rate");toggle(measure,"heart.avg","Average heart rate")
  toggle(measure,"section.cadence","Cadence section");toggle(measure,"cadence.current","Current cadence");toggle(measure,"cadence.max","Max cadence");toggle(measure,"cadence.avg","Average cadence")
  connect.addView(txt("Speed and distance source",13,if(dark)Color.WHITE else Color.DKGRAY,true))
  connect.addView(txt("Phone GPS (internal source)",12,if(dark)Color.LTGRAY else Color.DKGRAY,false))
  connect.addView(txt("Heart-rate source",13,if(dark)Color.WHITE else Color.DKGRAY,true))
  connect.addView(txt("Gear S3 via Bluetooth LE, if the watch exposes the standard Heart Rate Service.",12,if(dark)Color.LTGRAY else Color.DKGRAY,false))
  btn(connect,"Scan Bluetooth heart-rate device"){scan()}
  connect.addView(txt("Cadence source",13,if(dark)Color.WHITE else Color.DKGRAY,true))
  connect.addView(txt("Unavailable — no cadence sensor configured.",12,if(dark)Color.LTGRAY else Color.DKGRAY,false))
  val light=CheckBox(this).apply{text="Use light theme";isChecked=!dark};style.addView(light)
  val compactToggle=CheckBox(this).apply{text="Compact metric values";isChecked=compact};style.addView(compactToggle)
  style.addView(txt("Accent color",13,if(dark)Color.WHITE else Color.DKGRAY,true))
  val hex=EditText(this).apply{setSingleLine(true);setText(String.format("#%06X",0xFFFFFF and accent));hint="#RRGGBB"}
  val preview=View(this).apply{contentDescription="Selected accent color preview";background=GradientDrawable().apply{setColor(accent);cornerRadius=dp(4).toFloat();setStroke(dp(1),if(dark)Color.LTGRAY else Color.DKGRAY)}}
  val wheelRow=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER_VERTICAL}
  fun showPreview(color:Int){preview.background=GradientDrawable().apply{setColor(color);cornerRadius=dp(4).toFloat();setStroke(dp(1),if(dark)Color.LTGRAY else Color.DKGRAY)}}\n  val wheel=ColorWheelView(this){color->hex.setText(String.format("#%06X",0xFFFFFF and color));showPreview(color)}
  wheelRow.addView(wheel,LinearLayout.LayoutParams(0,dp(128),1f))
  val previewCol=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER}
  previewCol.addView(txt("Selected",11,if(dark)Color.WHITE else Color.DKGRAY,true))
  previewCol.addView(preview,LinearLayout.LayoutParams(dp(54),dp(54)).apply{topMargin=dp(5)})
  wheelRow.addView(previewCol,LinearLayout.LayoutParams(dp(78),dp(128)))
  style.addView(wheelRow)
  style.addView(hex)
  val swatches=LinearLayout(this).apply{orientation=0}
  listOf(Color.CYAN,Color.BLUE,Color.MAGENTA,Color.YELLOW,Color.WHITE).forEach{color->
   val swatch=TextView(this).apply{text="●";textSize=24f;gravity=Gravity.CENTER;setTextColor(color);setOnClickListener{hex.setText(String.format("#%06X",0xFFFFFF and color));showPreview(color)}}
   swatches.addView(swatch,LinearLayout.LayoutParams(0,dp(36),1f))
  };style.addView(swatches)
  fun showPage(index:Int){pages.forEachIndexed{i,p->p.visibility=if(i==index)View.VISIBLE else View.GONE};for(i in 0 until tabs.childCount){val t=tabs.getChildAt(i) as TextView;t.setTextColor(if(i==index)accent else if(dark)Color.LTGRAY else Color.DKGRAY)}}
  listOf("Measure","Connect","Style").forEachIndexed{i,label->val tab=TextView(this).apply{text=label;gravity=Gravity.CENTER;textSize=13f;setPadding(dp(5),dp(10),dp(5),dp(10));setOnClickListener{showPage(i)}};tabs.addView(tab,LinearLayout.LayoutParams(0,dp(40),1f))}
  root.addView(tabs)
  pages.forEach{root.addView(it,LinearLayout.LayoutParams(-1,-2))}
  showPage(0)
  ridePanel("Ride computer settings",root,listOf(
   "Cancel" to {render()},
   "Save" to {
    v.clear();v.putAll(draft);dark=!light.isChecked;compact=compactToggle.isChecked
    try{accent=Color.parseColor(hex.text.toString().trim())}catch(_:Exception){}
    val e=p.edit().putBoolean("dark",dark).putBoolean("compact",compact).putInt("accent",accent)
    v.forEach{(k,x)->e.putBoolean(k,x)};e.apply();render()
   }
  ))
 }
 private fun ridePanel(title:String,body:View,actions:List<Pair<String,()->Unit>>){
  val dialog=android.app.Dialog(this)
  val panel=LinearLayout(this).apply{orientation=1;setPadding(dp(10),dp(8),dp(10),dp(8));setBackgroundColor(if(dark)Color.BLACK else Color.rgb(246,248,250))}
  panel.addView(txt(title,17,if(dark)Color.WHITE else Color.DKGRAY,true))
  val sc=ScrollView(this);sc.addView(body);panel.addView(sc,LinearLayout.LayoutParams(-1,0,1f).apply{topMargin=dp(6)})
  val footer=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER}
  actions.forEach{(label,action)->val button=TextView(this).apply{text=label;gravity=Gravity.CENTER;textSize=12f;setTextColor(accent);setPadding(dp(3),dp(10),dp(3),dp(10));setOnClickListener{action();dialog.dismiss()}};footer.addView(button,LinearLayout.LayoutParams(0,dp(42),1f))}
  panel.addView(footer)
  dialog.setContentView(panel)
  dialog.setCancelable(false)
  dialog.show()
  val win=dialog.window
  if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);val width=((resources.displayMetrics.widthPixels-dp(24))/3).coerceAtLeast(dp(220));win.setLayout(width,android.view.WindowManager.LayoutParams.MATCH_PARENT);win.setGravity(Gravity.RIGHT);val lp=win.attributes;lp.x=dp(10);lp.y=0;win.attributes=lp}
 }
 private fun tripActions(){val content=LinearLayout(this).apply{orientation=1};content.addView(txt("Save Trip stores this ride in history. Delete Trip resets the active ride without saving it. Previously saved trips and global totals are retained.",13,if(dark)Color.LTGRAY else Color.DKGRAY,false));ridePanel("Trip actions",content,listOf("Save Trip" to {db.saveTrip(distance,secs,maxSpeed,if(speedN>0)speedSum/speedN else null,maxHeart,if(heartN>0)heartSum.toDouble()/heartN else null,maxCadence,if(cadenceN>0)cadenceSum.toDouble()/cadenceN else null);resetTrip();render()},"Delete Trip" to {resetTrip();render()},"Cancel" to {}))}
 private fun resetTrip(){distance=0.0;secs=0;maxSpeed=0.0;speedSum=0.0;speedN=0;heart=null;maxHeart=null;heartSum=0;heartN=0;cadence=null;maxCadence=null;cadenceSum=0;cadenceN=0;speed=null;prev=null;saveData()}
 private fun globals(){val msg="Total distance: "+String.format("%.2f km",gd)+"\nActive time: "+time(gs)+"\nAverage speed: "+(if(gn>0)String.format("%.1f km/h",gsum/gn)else"—")+"\nMax speed: "+String.format("%.1f km/h",gmax)+"\nMax heart rate: "+(ghmax?.let{"$it bpm"}?:"—")+"\nAverage heart rate: "+(if(ghn>0)(ghs/ghn).toString()+" bpm"else"—");val content=LinearLayout(this).apply{orientation=1};content.addView(txt(msg,13,if(dark)Color.LTGRAY else Color.DKGRAY,false));ridePanel("Global statistics",content,listOf("Close" to {}))}
 private fun resetConfirm(){AlertDialog.Builder(this,dt()).setTitle("Reset active trip?").setMessage("Saved history and global totals are retained.").setNegativeButton("Cancel",null).setPositiveButton("Reset"){_,_->resetTrip();render()}.show()}
 private fun history(){val trips=db.listTrips();val msg=if(trips.isEmpty())"No saved trips."else trips.joinToString("\n\n"){it.date+"\n"+String.format("%.2f km",it.distance)+" · "+time(it.seconds)+" · max "+String.format("%.1f km/h",it.maxSpeed)};AlertDialog.Builder(this,dt()).setTitle("Saved trips").setMessage(msg).setPositiveButton("Close",null).show()}
 private fun scan(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT),31);Toast.makeText(this,"Grant Bluetooth permission and scan again.",Toast.LENGTH_LONG).show();return};val a=BluetoothAdapter.getDefaultAdapter();if(a==null||!a.isEnabled){Toast.makeText(this,"Turn Bluetooth on first.",Toast.LENGTH_LONG).show();return};val scanner=a.bluetoothLeScanner?:return;val found=linkedMapOf<String,BluetoothDevice>();val cb=object:ScanCallback(){override fun onScanResult(t:Int,r:ScanResult){found[r.device.address]=r.device;runOnUiThread{val ds=found.values.toList();AlertDialog.Builder(this@MainActivity,dt()).setTitle("BLE heart-rate devices").setItems(ds.map{try{(it.name?:"Bluetooth device")+" · "+it.address}catch(_:Exception){"Bluetooth device"}}.toTypedArray()){_,i->connect(ds[i])}.setNegativeButton("Keep scanning",null).show();try{scanner.stopScan(this)}catch(_:Exception){}}};override fun onScanFailed(e:Int){runOnUiThread{Toast.makeText(this@MainActivity,"Scan failed: "+e,Toast.LENGTH_LONG).show()}}};try{scanner.startScan(listOf(ScanFilter.Builder().setServiceUuid(android.os.ParcelUuid(svc)).build()),ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),cb);Toast.makeText(this,"Scanning for standard BLE heart-rate devices.",Toast.LENGTH_LONG).show();h.postDelayed({try{scanner.stopScan(cb)}catch(_:Exception){}},12000)}catch(_:SecurityException){Toast.makeText(this,"Bluetooth permission required.",Toast.LENGTH_LONG).show()}}
 @SuppressLint("MissingPermission") private fun connect(d:BluetoothDevice){try{gatt?.close();gatt=d.connectGatt(this,false,object:BluetoothGattCallback(){override fun onConnectionStateChange(g:BluetoothGatt,s:Int,state:Int){if(state==BluetoothProfile.STATE_CONNECTED)g.discoverServices()else if(state==BluetoothProfile.STATE_DISCONNECTED)runOnUiThread{heart=null;render()}};override fun onServicesDiscovered(g:BluetoothGatt,s:Int){val c=g.getService(svc)?.getCharacteristic(hrid);if(c==null){runOnUiThread{Toast.makeText(this@MainActivity,"Device lacks standard heart-rate measurement.",Toast.LENGTH_LONG).show()};return};g.setCharacteristicNotification(c,true);val dsc=c.getDescriptor(cccd);if(dsc!=null){if(Build.VERSION.SDK_INT>=33)g.writeDescriptor(dsc,BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)else{@Suppress("DEPRECATION") dsc.value=BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;@Suppress("DEPRECATION") g.writeDescriptor(dsc)}}};@Deprecated("Deprecated in Java") override fun onCharacteristicChanged(g:BluetoothGatt,c:BluetoothGattCharacteristic){got(c.value)};override fun onCharacteristicChanged(g:BluetoothGatt,c:BluetoothGattCharacteristic,b:ByteArray){got(b)};private fun got(b:ByteArray?){if(b==null||b.size<2)return;val n=if((b[0].toInt() and 1)!=0&&b.size>=3)(b[1].toInt() and 255)or((b[2].toInt() and 255)shl 8)else b[1].toInt() and 255;runOnUiThread{heart=n;if((speed?:0.0)>0){maxHeart=max(maxHeart?:n,n);heartSum+=n;heartN++;ghmax=max(ghmax?:n,n);ghs+=n;ghn++};saveData();render()}}})}catch(_:SecurityException){Toast.makeText(this,"Bluetooth permission required.",Toast.LENGTH_LONG).show()}}

 private fun commandDialog(){val input=EditText(this).apply{hint="Try GPS, Spotify, pulsar, YouTube, Samsung Health, Settings";setSingleLine(true)};AlertDialog.Builder(this,dt()).setTitle("Test command").setMessage("Typed command testing; this does not use the microphone.").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Run"){_,_->val phrase=input.text.toString().trim().lowercase();val target=when{phrase.contains("spotify")->"Spotify";phrase.contains("pulsar")->"pulsar";phrase.contains("youtube")->"YouTube";phrase.contains("health")->"Samsung Health";phrase.contains("setting")->"Settings";phrase.contains("gps")||phrase.contains("map")->"GPS";else->null};if(target!=null){module=target;render();Toast.makeText(this,"Opened "+target,Toast.LENGTH_SHORT).show()}else Toast.makeText(this,"Command not recognized or not implemented yet.",Toast.LENGTH_LONG).show()}.show()}

 private fun icon(p:LinearLayout,g:String,label:String,c:Int,a:()->Unit){val q=TextView(this).apply{text=g;textSize=21f;gravity=Gravity.CENTER;setTextColor(c);contentDescription=label;setBackgroundColor(Color.TRANSPARENT);background=null;foreground=null;setPadding(0,0,0,0);setOnClickListener{a()}};p.addView(q,LinearLayout.LayoutParams(0,dp(38),1f).apply{if(p.childCount>0)leftMargin=dp(4)})}
 private fun btn(p:LinearLayout,s:String,a:()->Unit){p.addView(Button(this).apply{text=s;textSize=11f;setOnClickListener{a()}},LinearLayout.LayoutParams(0,-2,1f).apply{rightMargin=dp(3)})}
 private fun txt(s:String,n:Int,c:Int,b:Boolean)=TextView(this).apply{text=s;textSize=n.toFloat();setTextColor(c);if(b)setTypeface(typeface,Typeface.BOLD)}
 private fun shape(c:Int,r:Int,b:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat();setStroke(dp(1),b)}
 private fun dt()=if(dark)android.R.style.Theme_Material_Dialog_Alert else android.R.style.Theme_Material_Light_Dialog_Alert
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun time(s:Long)=String.format("%02d:%02d:%02d",s/3600,(s%3600)/60,s%60)
}