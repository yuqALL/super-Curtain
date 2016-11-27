/*
 * 通讯需求
 * 1.解析控制命令
 *    0   置为自动模式
 *    1   置为手动模式
 *    3   开窗帘，前提是手动模式下
 *    4   关窗帘，前提是手动模式下
 *    5   设置温度阀值
 *    6   设置湿度阀值
 *    7   设置亮度阀值
 *
 *2.传送传感器信息
 *    传回温度信息
 *    传回湿度信息
 *    传回亮度信息
 *    传回窗帘开关状态
 *    传回窗帘控制模式信息
 *
 *3 连接后回送菜单
 */

//舵机头文件
#include <Servo.h>
#include <SHT1x.h>

//宏定义
#define LIGHT_PIN        A0         //光强检测输出引脚
#define LIGHT_THRESHOLD    300    //光强阀值
#define dataPin  8
#define clockPin 7

//定义变量并赋初值
SHT1x sht1x(dataPin, clockPin);
String mes = "";
int control_mode = 0; // 0 自动  1 手动
String controlMes = "";//接收控制指令
int which = 0; //设置有关阀值  0  温度阀值  1 湿度阀值  2  光阀值
int curtain_state = 0; // 0 关  1 开
float tempture_threshold = 25;
float humidity_threshold = 60;
float light_threshold = 1000;

float current_tempture = 0;
float current_humidity = 0;
float current_light = 0;

boolean daylight = false;
boolean warm = true;
boolean bumanzu = true;
boolean flag = false;

//创建两个舵机控制对象
Servo myservo;
Servo myserv1;

//函数定义
int  forward(void)                          //正转
{ myservo.write(0);
  myserv1.write(180);
  Serial.println( myservo.read());
}

int  reverse(void)                          //反转
{ myservo.write(180);
  myserv1.write(0);
  Serial.println( myservo.read());
}

int doStop(void)                           //静止
{ myservo.write(90);
  myserv1.write(90);
}

void sendMes(String mes)
{
  //符号表示消息头
  Serial.print("&");
  //两位数字表示数据长度--------发现这个并没有卵用，得到的数字可能只有一位
  //   int mesSize=mes.length();
  //   if(mesSize<10) Serial.print(0);
  //   Serial.print(mesSize);
  Serial.print(mes);
  //消息尾
  Serial.print("%");
  Serial.print("\n");
  delay(20);
}
//0  tempture threshold 1 humidity 2 light 3 4 5获取传感器数据
//这里消息格式与手机端解析对应，：号左边为字符串且空一格，右边空一格然后接传递的数字
void sendDataMes(int which, float value)
{
  char   c[25];
  dtostrf(value, 4, 2, c);
  //符号表示消息头
  Serial.print("&");
  if (which == 0)
  {
    Serial.print("Tempture threshold : ");
    Serial.print(c);
  } else if (which == 1) {
    Serial.print("Humidity threshold : ");
    Serial.print(c);
  } else if (which == 2) {
    Serial.print("Light threshold : ");
    Serial.print(c);
  } else if (which == 3) {
    Serial.print("environment tempture : ");
    Serial.print(c);
  } else if (which == 4) {
    Serial.print("environment humidity : ");
    Serial.print(c);
  } else if (which == 5) {
    Serial.print("environment light : ");
    Serial.print(c);
  }

  //消息尾
  Serial.print("%");
  Serial.print("\n");
  delay(20);
}

void analyzeMes(String mes)
{
  int index = mes.indexOf(":");
  String meshead;
  float value;

  if (index != -1)
  {
    meshead = mes.substring(0, index);
    value = mes.substring(index + 1).toFloat();
    if (meshead == "5")
    {
      tempture_threshold = value;
      sendMes(mes);
      sendDataMes(0, value);
    } else if (meshead == "6")
    {
      humidity_threshold = value;
      sendMes(mes);
      sendDataMes(1, value);
    } else if (meshead == "7")
    {
      light_threshold = value;
      sendMes(mes);
      sendDataMes(2, value);
    }
  } else if (mes == "help" || mes == "Help")
  {
    sendMes("Help$ menu here ");
    sendMes("0$ Change control mode to auto");
    sendMes("1$ Change control mode to manual");
    sendMes("3$ close the curtain");
    sendMes("4$ open the curtain");
    sendMes("5$ Set the tempture threshold");
    sendMes("6$ Set the humidity threshold");
    sendMes("7$ Set the light threshold");
    sendMes("8$ Get the curtain state");
    sendMes("9$ Get the control mode");
    sendMes("a$ Get the environment tempture");
    sendMes("b$ Get the environment humidity");
    sendMes("c$ Get the environment light intensity");
    sendMes("d$ Get the tempture threshold");
    sendMes("e$ Get the humidity threshold");
    sendMes("f$ Get the light intensity threshold");
    sendMes("Help or help$ Get the help");
  } else {
    sendMes("You send me message $ " + mes);
    sendMes("I don't know what you want !");
    sendMes("Please send 'help' or 'Help' to get option menu");
  }

}

void setup() {
  myservo.attach(9);              //舵机1由arduino第九脚控制
  myserv1.attach(10);            //舵机2由arduino第十脚控制
  Serial.begin(9600);
  
}

void loop() {
  while (Serial.available())                 //当前串口缓冲池的数据量
  {
    mes += char(Serial.read());           //读取手机发送过来的字符，并存储
    delay(2);
  }
  if (mes.length() == 1) {                //对手机发送字符长度判断，单字符节执行下面函数
    if (mes == "0") {
      sendMes("automatic mode");
      control_mode = 0;
      delay(2);

    } else if (mes == "1") {
      sendMes("manual mode");
      control_mode = 1;
      delay(2);

    } else if (mes == "3") {
      sendMes("Try to close the curtain");
      if (control_mode == 1) controlMes = mes;
      delay(2);
    } else if (mes == "4") {
      sendMes("Try to open the curtain");
      if (control_mode == 1) controlMes = mes;
      delay(2);
    }
    else if (mes == "5") {
      sendMes("You should send the tempture threshold value");
    } else if (mes == "6") {
      sendMes("You should send the humidity threshold value");
    } else if (mes == "7") {
      sendMes("You should send the light threshold value");
    } else if (mes == "8") {
      if (curtain_state == 0) {
        sendMes("Curtain had closed !");
      } else if (curtain_state == 1) {
        sendMes("Curtain had opened !");
      }
    } else if (mes == "9") {
      if (control_mode == 0) {
        sendMes("The control mode is auto");
      } else if (control_mode == 1) {
        sendMes("The control mode is manual");
      }
    } else if (mes == "a") {
      sendDataMes(3, current_tempture);
    } else if (mes == "b") {
      sendDataMes(4, current_humidity);
    } else if (mes == "c") {
      sendDataMes(5, current_light);
    } else if (mes == "d") {
      sendDataMes(0, tempture_threshold);
    } else if (mes == "e") {
      sendDataMes(1, humidity_threshold);
    } else if (mes == "f") {
      sendDataMes(2, light_threshold);
    }
    mes = "";                        //mes字符串初始化
  } else if (mes.length() > 1) {
    analyzeMes(mes);
    mes = "";
  }
  //读取光强
  current_light = analogRead(LIGHT_PIN);
  delay(200);
  sendDataMes(5, current_light);

  // 读取湿度
  current_humidity = sht1x.readHumidity();
  delay(200);
  sendDataMes(4, current_humidity);

  //检测温度，并将电压转换到摄氏温度
  current_tempture = sht1x.readTemperatureC();
  delay(200);
  sendDataMes(3, current_tempture);

  //判断光强与阀值得大小，对daylight进行赋值
  if (current_light > light_threshold)
    daylight = true;
  else
    daylight = false;
  //判断温度与阀值得大小，对daylight进行赋值
  if (current_tempture > tempture_threshold)
    warm = true;
  else
    warm = false;

  if (!daylight || !warm == 1)
    bumanzu = 1;
  else
    bumanzu = 0;

  if (control_mode == 1) {    //手动控制模式下基本控制
    if (controlMes == "3") {
      if (curtain_state == 0) {
        sendMes("Curtain had closed !");
      } else if (curtain_state == 1) {
        sendMes("Closing the curtain !");
        reverse();
        delay(1000);
        doStop();
        sendMes("Closed the curtain !");
        curtain_state = 0;
      }
    } else if (controlMes == "4") {
      if (curtain_state == 0)  //判断窗帘状态，防止循环下重复开窗帘
      {
        sendMes("Opening the curtain");
        //尝试开窗帘...
        forward();
        delay(1000);
        doStop();
        sendMes("Opened the curtain !");
        curtain_state = 1;
      } else {
        sendMes("Curtain had opened !");
      }
    } else if (controlMes == "5") {
    } else if (controlMes == "6") {
    } else if (controlMes == "7") {
    }
  } else if (control_mode == 0) { //自动控制模式下，基本处理
    if (daylight && warm)  //光线和温度满足条件后控制窗帘状态
    {
      if (curtain_state == 0)  //判断窗帘状态，防止循环下重复开窗帘
      {
        sendMes("Opening the curtain");
        //尝试开窗帘...
        forward();
        delay(1000);
        doStop();
        sendMes("Opened the curtain !");
        curtain_state = 1;
      }
      else
      {
        sendMes("Curtain had opened !");
      }
    } else {
      if (curtain_state == 1) //判断窗帘状态，防止循环下重复开窗帘
      {
        sendMes("Closing the curtain");
        //尝试关窗帘
        reverse();
        delay(1000);
        doStop();
        sendMes("Closed the curtain !");
        curtain_state = 0;
      } else
      {
        sendMes("Curtain had closed !");
      }
    }
  }
}


