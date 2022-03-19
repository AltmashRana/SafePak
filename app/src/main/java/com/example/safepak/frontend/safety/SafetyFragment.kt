package com.example.safepak.frontend.safety



import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.safepak.data.User
import com.example.safepak.databinding.FragmentSafetyBinding
import com.example.safepak.frontend.chat.ChatBoxActivity
import com.example.safepak.frontend.chat.ChatsFragment
import com.example.safepak.frontend.safetyAdapters.CloselistItem
import com.google.firebase.firestore.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_safety.*
import java.lang.Exception
import com.example.safepak.R
import com.example.safepak.frontend.blood.BloodBroadcastActivity
import com.example.safepak.frontend.maps.LocationActivity
import com.example.safepak.frontend.safetyAdapters.HelplistItem
import com.example.safepak.frontend.safetyAdapters.UnknownHelplistItem
import com.example.safepak.frontend.services.CameraService
import com.example.safepak.logic.models.Call
import com.google.android.gms.location.*
import java.util.HashMap

import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.session.EmergencySession
import com.example.safepak.logic.session.EmergencySession.getCurrentCall
import com.example.safepak.logic.session.EmergencySession.initiateLevel1
import com.example.safepak.logic.session.EmergencySession.initiateLevel2
import com.example.safepak.logic.session.EmergencySession.requestLocation
import com.example.safepak.logic.session.EmergencySession.stopCall
import com.example.safepak.logic.session.EmergencySession.stopLocationService
import com.example.safepak.logic.session.EmergencySession.updateUserLocationInFirebase
import com.example.safepak.logic.session.LocalDB
import com.google.firebase.database.*
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.preference.PreferenceManager
import com.example.safepak.logic.session.EmergencySession.isCameraServiceRunning


class SafetyFragment : Fragment() {

    private val closelistAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var close_recycler: RecyclerView

    private val helplistAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var help_recycler: RecyclerView
    private val unkownhelplistAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var unknownhelp_recycler: RecyclerView
    private val latestCallsMap = HashMap<String, Pair<User,Call>>()
    private val latestUnknownCallsMap = HashMap<String, Pair<Call, Pair<User, Boolean>>>()

    private lateinit var db: FirebaseFirestore

    private lateinit var currentuser: User
    private var level1_flag = 0
    private var level1Cancel_flag = 0

    private var level2_flag = 0
    private var level2Cancel_flag = 0

    private var current_call: Call? = null

    private var address: String? = null
    private lateinit var myLocation : Location


    lateinit var binding: FragmentSafetyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSafetyBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()

        FirebaseSession.getCurrentUser { user ->
            currentuser = user
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadHelplist()
        loadUnknownHelplist()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        current_call = getCurrentCall(requireContext())
        if (current_call != null){
            if(current_call?.type == "level1") {
                binding.level1Bt.setImageResource(R.drawable.cancel_ic)
                level1_flag = 1
            }
            else if(current_call?.type == "level2"){
                binding.level2Bt.setImageResource(R.drawable.cancel_ic)
                level2_flag = 1
            }
        }

        close_recycler = binding.closeRecycler
        close_recycler.adapter = closelistAdapter

        help_recycler = binding.helpRecycler
        help_recycler.adapter = helplistAdapter

        unknownhelp_recycler = binding.unknownhelpRecycler
        unknownhelp_recycler.adapter = unkownhelplistAdapter

        loadCloselist()

        updateGPS{}

        binding.emptycloseText.visibility = View.VISIBLE

        binding.emptyhelpText.visibility = View.VISIBLE

        binding.emptyunknownhelpText.visibility = View.VISIBLE

        binding.refreshsafetySwipe.setOnRefreshListener {
            binding.emptyhelpText.visibility = View.VISIBLE
            binding.emptyhelpText.visibility = View.VISIBLE
            binding.emptyunknownhelpText.visibility = View.VISIBLE
            loadCloselist()
            loadHelplist()
            loadUnknownHelplist()
        }

        binding.level1Bt.setOnClickListener {
            if (level1_flag == 0 && level1Cancel_flag == 0 && level2_flag == 0 && level2Cancel_flag == 0) {
                if (EmergencySession.isLocationEnabled(view.context)) {

                    if (ContextCompat.checkSelfPermission(view.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        requestLocation(requireContext())
                        updateGPS { result ->
                            if (result != null) {

                                level1_bt.setImageResource(R.drawable.cancel_ic)
                                level1Cancel_flag = 1

                                Toast.makeText(context, "Level-1 call will be initiated in 5 secs", Toast.LENGTH_SHORT).show()

                                Thread {
                                    Thread.sleep(5000)
                                    activity?.runOnUiThread {
                                        Runnable {
                                            if (level1Cancel_flag == 1) {
                                                binding.level1Bt.isEnabled = false
                                                YoYo.with(Techniques.Tada)
                                                    .duration(400)
                                                    .repeat(1)
                                                    .playOn(binding.level1Bt)

                                                EmergencySession.startLocationService(context)
                                                initiateLevel1(requireContext(), result)
                                                level1Cancel_flag = 0
                                                level1_flag = 1
                                                Toast.makeText(context, "Level-1 emergency initiated!", Toast.LENGTH_SHORT).show()
                                                binding.level1Bt.isEnabled = true
                                            }
                                        }.run()
                                    }
                                }.start()
                            }
                        }
                    }
                    else{
                        requestPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                } else
                    Toast.makeText(context, "Call Failed!, Please turn on location", Toast.LENGTH_SHORT).show()
            } else if (level1Cancel_flag == 1) {
                level1Cancel_flag = 0
                binding.level1Bt.setImageResource(R.drawable.level1)

                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            } else if (level1_flag == 1) {
                stopLocationService(requireContext())
                stopCall(requireContext())
                level1_flag = 0
                binding.level1Bt.setImageResource(R.drawable.level1)

                Toast.makeText(context, "Call Stopped", Toast.LENGTH_SHORT).show()
            }
        }

        binding.medicalBt.setOnClickListener {
            if (level1_flag == 0 && level1Cancel_flag == 0 && level2_flag == 0 && level2Cancel_flag == 0) {
                val intent = Intent(view.context, BloodBroadcastActivity::class.java)
                startActivity(intent)
            }
        }

        binding.level2Bt.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val switch = prefs?.getBoolean("level2_cam", false)

            if (level1_flag == 0 && level1Cancel_flag == 0 && level2_flag == 0 && level2Cancel_flag == 0 ) {
                if (EmergencySession.isLocationEnabled(view.context)) {
                    if (ContextCompat.checkSelfPermission(view.context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        requestLocation(requireContext())
                            updateGPS{ result ->
                                if (result != null) {
                                    level2_bt.setImageResource(R.drawable.cancel_ic)
                                    level2Cancel_flag = 1
                                    Toast.makeText(context, "Level-2 call will be initiated in 3 secs", Toast.LENGTH_SHORT).show()
                                    Thread {
                                        Thread.sleep(3000)
                                            Runnable {
                                                if (level2Cancel_flag == 1) {
                                                    activity?.runOnUiThread {
                                                        binding.level2Bt.isEnabled = false
                                                        YoYo.with(Techniques.Tada)
                                                            .duration(400)
                                                            .repeat(1)
                                                            .playOn(binding.level2Bt)
                                                        binding.level2Bt.setImageResource(R.drawable.cancel_ic)
                                                        level2Cancel_flag = 0
                                                        level2_flag = 1
                                                        Toast.makeText(context, "Level-2 emergency initiated!", Toast.LENGTH_SHORT).show()
                                                        binding.level2Bt.isEnabled = true

                                                    EmergencySession.startLocationService(context)
                                                    initiateLevel2(requireContext(), result)

                                                    if (ContextCompat.checkSelfPermission(
                                                            requireView().context,
                                                            Manifest.permission.CAMERA
                                                        ) == PackageManager.PERMISSION_GRANTED
                                                        && ContextCompat.checkSelfPermission(
                                                            requireView().context,
                                                            Manifest.permission.RECORD_AUDIO
                                                        ) == PackageManager.PERMISSION_GRANTED
                                                        && ContextCompat.checkSelfPermission(
                                                            requireView().context,
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                        ) == PackageManager.PERMISSION_GRANTED
                                                    //                    && ContextCompat.checkSelfPermission(requireView().context, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED
                                                    ) {
                                                        if(switch == true){
                                                            val intent = Intent(context, CameraService::class.java)
                                                            intent.putExtra("Front_Request", true)
                                                            context?.startForegroundService(intent)
                                                        }
                                                    } else {
                                                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                        requestCameraPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                        requestCameraPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                        requestCameraPermissionLauncher.launch(Manifest.permission.SYSTEM_ALERT_WINDOW)
                                                        checkStartPermissionRequest()
                                                    }
                                                }
                                            }
                                            }.run()
                                    }.start()
                                }
                            }
                    }
                    else{
                        requestPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                } else
                    Toast.makeText(context, "Call Failed!, Please turn on location", Toast.LENGTH_SHORT).show()
            } else if (level2Cancel_flag == 1) {
                level2Cancel_flag = 0
                binding.level2Bt.setImageResource(R.drawable.level2)

                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
            } else if (level2_flag == 1) {
                stopCall(requireContext())
                stopLocationService(requireContext())
                if (requireContext().isCameraServiceRunning(CameraService::class.java)){
                    val intent = Intent(context, CameraService::class.java)
                    context?.stopService(intent)
                }
                level2_flag = 0
                binding.level2Bt.setImageResource(R.drawable.level2)

                Toast.makeText(context, "Call Stopped", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                updateGPS {

                }
            } else {
                activity?.finish()
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

            } else {
                Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 101
    fun checkStartPermissionRequest(): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context?.packageName)
            )
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            return false // above will start new Activity with proper app setting
        }
        return true // on lower OS versions granted during apk installation
    }


    private fun loadCloselist() {
        closelistAdapter.clear()
            FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}")
                .get().addOnSuccessListener { friend ->
                    friend.children.forEach{
                        val friendship = it.getValue(Friendship::class.java)
                        if (friendship?.status == "close") {
                            val get_user = FirebaseFirestore.getInstance().collection("users").document(friendship.friendid!!)
                            get_user.get().addOnSuccessListener { doc ->
                                val user = doc.toObject(User::class.java)!!
                                closelistAdapter.add(CloselistItem(user))
                                binding.emptycloseText.visibility = View.GONE
                            }
                        }
                    }
                    binding.refreshsafetySwipe.isRefreshing = false
                }
        closelistAdapter.setOnItemClickListener { item, view ->
            val userItem = item as CloselistItem
            val intent = Intent(view.context, ChatBoxActivity::class.java)
            intent.putExtra(ChatsFragment.USER_KEY, userItem.user)
            intent.putExtra("IS_FRIEND", true)
            startActivity(intent)
        }
    }

    private fun loadHelplist() {
        FirebaseDatabase.getInstance().getReference("/friends")
            .get().addOnSuccessListener { users ->
                users.children.forEach{ docs ->
                    if (docs.key != FirebaseSession.userID) {
                        docs.children.forEach {
                            val friendship = it.getValue(Friendship::class.java)
                            if (friendship?.friendid == FirebaseSession.userID && friendship?.status == "close") {
                                val get_user = FirebaseFirestore.getInstance().collection("users")
                                    .document(friendship.userid!!)
                                get_user.get().addOnSuccessListener { doc ->
                                    val user = doc.toObject(User::class.java)!!
                                    getCloseCalls(user)
                                }
                            }
                        }
                    }
                }
                helplistAdapter.setOnItemClickListener{ item, view ->
                    val userItem = item as HelplistItem
                    val intent = Intent(view.context, LocationActivity::class.java)
                    intent.putExtra("user", userItem.data.first)
                    intent.putExtra("call_id", userItem.data.second.id)
                    intent.putExtra("is_friend", true)
                    startActivity(intent)
                }
            }
    }

    private fun refreshRecyclerHelplist() {
        helplistAdapter.clear()
        latestCallsMap.values.forEach {
            helplistAdapter.add(HelplistItem(it))
        }
    }

    private fun getCloseCalls(user: User) {
        val latest_query = FirebaseDatabase.getInstance().reference.child("/emergency-calls/${user.userid}")

        latest_query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                p0.getValue(Call::class.java)?.let {
                    if (it.status == "going") {
                        latestCallsMap[it.id!!] = Pair(user, it)
                        refreshRecyclerHelplist()
                        binding.emptyhelpText.visibility = View.GONE
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                p0.getValue(Call::class.java)?.let {
                    if (it.status == "stopped") {
                        latestCallsMap.remove(it.id!!)
                        refreshRecyclerHelplist()
                        binding.emptyhelpText.visibility = View.GONE
                    }
                }
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }

    private fun loadUnknownHelplist() {
        val responses = LocalDB.getResponses(requireContext())

        responses?.forEach{
            FirebaseFirestore.getInstance().collection("users")
            .document(it.second).get().addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)!!
                    getUnknownCalls(user, it.first)
                }
            }
        unkownhelplistAdapter.setOnItemClickListener{ item, view ->
            val userItem = item as UnknownHelplistItem
            val intent = Intent(view.context, LocationActivity::class.java)
            intent.putExtra("user", userItem.data.second.first)
            intent.putExtra("call_id", userItem.data.first.id)
            intent.putExtra("is_friend", userItem.data.second.second)
            startActivity(intent)
        }
    }

    private fun refreshRecyclerUnkownHelplist() {
        unkownhelplistAdapter.clear()
        latestUnknownCallsMap.values.forEach {
            unkownhelplistAdapter.add(UnknownHelplistItem(it))
        }
    }

    private fun getUnknownCalls(user: User, callid : String) {
        val latest_query = FirebaseDatabase.getInstance().reference.child("/emergency-calls/${user.userid}/")

        latest_query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                p0.getValue(Call::class.java)?.let {
                    if(it.status == "going" && it.id == callid) {
                        FirebaseDatabase.getInstance()
                            .getReference("friends/${FirebaseSession.userID}/${user.userid}/")
                            .get().addOnSuccessListener { child ->
                                if(child.exists())
                                    latestUnknownCallsMap[it.id!!] = Pair(it , Pair(user, true))
                                else
                                    latestUnknownCallsMap[it.id!!] = Pair(it , Pair(user, false))
                            }
                        refreshRecyclerUnkownHelplist()
                        binding.emptyunknownhelpText.visibility = View.GONE
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                p0.getValue(Call::class.java)?.let {
                    if (it.status == "stopped" && it.id == callid)  {
                        latestUnknownCallsMap.remove(it.id!!)
                        refreshRecyclerUnkownHelplist()
                        binding.emptyunknownhelpText.visibility = View.GONE
                    }
                }
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }

    private fun updateGPS(onComplete: (UserLocation?) -> Unit) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireView().context)
            //we have permission from user
        var result : UserLocation? = null
        if (ContextCompat.checkSelfPermission(requireView().context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener{ location ->
                val geocoder = Geocoder(activity?.baseContext)
                try {
                    if(location != null) {
                        myLocation = location
                        address = geocoder.getFromLocation(location.latitude, location.longitude, 1)[0].getAddressLine(0).toString()
                        result = UserLocation(location.longitude.toString(), location.latitude.toString(), address)
                        updateUserLocationInFirebase(result)
                        onComplete(result)
                    } else
                        "Missing"
                } catch (e: Exception) {
                    Toast.makeText(context, "Address capture failed", Toast.LENGTH_SHORT).show()
                }
            } else{

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}