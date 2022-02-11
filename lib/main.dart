import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Flutter Demo Printer',
        theme: ThemeData(primarySwatch: Colors.blue),
        home: const PrinterDemoPage());
  }
}

class PrinterDemoPage extends StatefulWidget {
  const PrinterDemoPage({Key? key}) : super(key: key);

  @override
  _PrinterDemoPageState createState() => _PrinterDemoPageState();
}

class _PrinterDemoPageState extends State<PrinterDemoPage> {
  final ImagePicker _imagePicker = ImagePicker();
  final TextEditingController _textEditingController = TextEditingController();
  static const _platform =
      MethodChannel("flerma.flutter.printer_flutter_example/kotlin");

  Widget _inputText() {
    return TextField(
        controller: _textEditingController,
        maxLines: null,
        keyboardType: TextInputType.multiline,
        decoration: const InputDecoration(hintText: "Text to print"));
  }

  Widget _bodyPage() {
    return Column(children: [_inputText()]);
  }

  FloatingActionButton _fabPrintText() {
    return FloatingActionButton.extended(
        heroTag: "fab-print-text",
        onPressed: _printText,
        label: const Text("Print text"),
        icon: const Icon(Icons.text_fields));
  }

  FloatingActionButton _fabPrintImage() {
    return FloatingActionButton.extended(
        heroTag: "fab-print-image",
        onPressed: _printImage,
        label: const Text("Print an image"),
        icon: const Icon(Icons.image));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(centerTitle: true, title: const Text("Printer demo")),
        body: SafeArea(child: _bodyPage()),
        floatingActionButton: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              _fabPrintText(),
              const SizedBox(height: 10),
              _fabPrintImage()
            ]));
  }

  Future<void> _printText() async {
    try {
      await _platform
          .invokeMethod("print-text", {"text": _textEditingController.text});
    } catch (error) {}
  }

  Future<void> _printImage() async {
    try {
      final XFile? image =
          await _imagePicker.pickImage(source: ImageSource.gallery);
      if (image != null) {
        String image64 = base64Encode(await File(image.path).readAsBytes());
        await _platform.invokeMethod("print-image", {"image_base64": image64});
      }
    } catch (error) {}
  }
}
