package com.reactnativeprinterbrothers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.brother.sdk.lmprinter.Channel;
import com.brother.sdk.lmprinter.OpenChannelError;
import com.brother.sdk.lmprinter.PrintError;
import com.brother.sdk.lmprinter.PrinterDriver;
import com.brother.sdk.lmprinter.PrinterDriverGenerateResult;
import com.brother.sdk.lmprinter.PrinterDriverGenerator;
import com.brother.sdk.lmprinter.PrinterModel;
import com.brother.sdk.lmprinter.setting.CustomPaperSize;
import com.brother.sdk.lmprinter.setting.TDPrintSettings;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ReactModule(name = PrinterBrothersModule.NAME)
public class PrinterBrothersModule extends ReactContextBaseJavaModule {
  public static final String NAME = "BROTHER_PRINTER";

  public PrinterBrothersModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void connectPrinter(String address, final Promise promise) {
    Channel channel = Channel.newBluetoothChannel(address, BluetoothAdapter.getDefaultAdapter());
    System.out.println("Printer IP :" + address);
    Toast.makeText(getReactApplicationContext(), "Printer IP :" + address, Toast.LENGTH_LONG).show();

    PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(channel);
    if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {
      Toast.makeText(getReactApplicationContext(), result.getError().getCode() + "", Toast.LENGTH_LONG).show();
      promise.reject(String.valueOf(result.getError().getCode()), "Error while connecting printer");
    }
    PrinterDriver printerDriver = result.getDriver();
    if (printerDriver != null) {
      printerDriver.closeChannel();
      promise.resolve("Printer connected successfully.");
    } else {
      promise.resolve("Printer not connected.");
    }
  }

  @ReactMethod
  public void printImageWithBlueToothPrinter(String macAddress, String filePath, final Promise promise) {
    try {
      if (ActivityCompat.checkSelfPermission(getReactApplicationContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED) {

        Channel channel = Channel.newBluetoothChannel(macAddress, BluetoothAdapter.getDefaultAdapter());
        PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(channel);
        if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {
          Log.e(NAME, "Error - Open Channel: " + result.getError().getCode());
          promise.reject(result.getError().getCode() + "", result.getError().getErrorUserInfo() + "");
        }
        File dir = getReactApplicationContext().getExternalFilesDir(null);
        PrinterDriver printerDriver = result.getDriver();
        TDPrintSettings printSettings = new TDPrintSettings(PrinterModel.RJ_4230B);

        CustomPaperSize.Margins margins = new CustomPaperSize.Margins(0.0f, 0.0f, 0.0f, 0.0f);
        CustomPaperSize customPaperSize = CustomPaperSize.newRollPaperSize(4.0f, margins,
          CustomPaperSize.Unit.Inch);

        printSettings.setCustomPaperSize(customPaperSize);
        printSettings.setWorkPath(dir.toString());

        PrintError printError = printerDriver.printPDF(filePath, printSettings);

        if (printError.getCode() != PrintError.ErrorCode.NoError) {
          printerDriver.closeChannel();
          promise.reject(printError.getCode() + "", result.getError().getErrorUserInfo() + "");
        } else {
          printerDriver.closeChannel();
          promise.resolve("Success - Print Image");
        }
      }
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  @SuppressLint("MissingPermission")
  @ReactMethod
  public void getConnectedBluetoothDevices(final Promise promise) {
    try {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter
        .getDefaultAdapter();
      if (bluetoothAdapter != null) {
        if (!bluetoothAdapter.isEnabled()) {
          Intent enableBtIntent = new Intent(
            BluetoothAdapter.ACTION_REQUEST_ENABLE);
          enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          ReactApplicationContext context = getReactApplicationContext();
          context.startActivity(enableBtIntent);
          return;
        }
      } else {
        return;
      }

      List<BluetoothDevice> connectedBlueToothDevices = getPairedBluetoothDevice(bluetoothAdapter);
      JSONArray jsonArray = new JSONArray();
      if (connectedBlueToothDevices.size() > 0) {
        for (BluetoothDevice device : connectedBlueToothDevices) {
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("name", device.getName());
          jsonObject.put("macAddress", device.getAddress());
          jsonArray.put(jsonObject);
        }
      }

      promise.resolve(jsonArray.toString());
    } catch (Exception e) {
      Log.d(NAME, "Printer Error: " + e.getMessage());
      promise.reject(e);
    }
  }

  @SuppressLint("MissingPermission")
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
  private List<BluetoothDevice> getPairedBluetoothDevice(BluetoothAdapter bluetoothAdapter) {
    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    if (pairedDevices == null || pairedDevices.size() == 0) {
      return new ArrayList<>();
    }
    ArrayList<BluetoothDevice> devices = new ArrayList<>();
    for (BluetoothDevice device : pairedDevices) {
      if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE) {
        devices.add(device);
      }
    }
    return devices;
  }
}
