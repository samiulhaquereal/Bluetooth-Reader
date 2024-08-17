import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Bluetooth List',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: BluetoothListScreen(),
    );
  }
}

class BluetoothListScreen extends StatefulWidget {
  @override
  _BluetoothListScreenState createState() => _BluetoothListScreenState();
}

class _BluetoothListScreenState extends State<BluetoothListScreen> {
  static const platform = MethodChannel('samples.flutter.dev/bluetooth');
  List<String> _bluetoothDevices = [];
  bool _isLoading = true; // To track loading state

  @override
  void initState() {
    super.initState();
    _getBluetoothDevices();
  }

  Future<void> _getBluetoothDevices() async {
    List<String> bluetoothDevices;
    try {
      final List<dynamic> result = await platform.invokeMethod('getBluetoothDevices');
      bluetoothDevices = List<String>.from(result);
    } on PlatformException catch (e) {
      bluetoothDevices = ['Failed to get Bluetooth devices: ${e.message}'];
    }

    setState(() {
      _bluetoothDevices = bluetoothDevices;
      _isLoading = false; // Stop loading once data is fetched
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Bluetooth Devices'),
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator()) // Show loading spinner while loading
          : ListView.builder(
        itemCount: _bluetoothDevices.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(_bluetoothDevices[index]),
          );
        },
      ),
    );
  }
}
