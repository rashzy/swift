package core;

import utils.FileManager;
import interfaces.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public abstract class Peer implements ITransferable {
  final String name;
  final String ip;
  final int port;

  public Peer(String name, String ip, int port) {
    this.name = name;
    this.ip = ip;
    this.port = port;
  }

  public abstract void start();
}

