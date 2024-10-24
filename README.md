# G-pilot IDEA扩展插件

## 简介

G-pilot是技术中心开发的代码补全工具，旨在为大家提供公司内网私有部署的、针对GS、c#、lua等语言特别优化过的代码补全工具。  
目前G-pilot已开发了VSCode、VS2022、jetbrains系列（IDEA、pycharm、Clion）等IDE插件，大家可以根据自己的需求选择使用。

G-pilot模型训练：[https://m68gitlab.g-bits.com/aigc/g-pilot](https://m68gitlab.g-bits.com/aigc/g-pilot)  
G-pilot vscode插件：[https://m68gitlab.g-bits.com/aigc/g-pilot-vscode](https://m68gitlab.g-bits.com/aigc/g-pilot-vscode)  
G-pilot vscode插件后端：[https://m68gitlab.g-bits.com/aigc/g-pilot-lsp](https://m68gitlab.g-bits.com/aigc/g-pilot-lsp)  
G-pilot VS扩展插件：[https://m68gitlab.g-bits.com/aigc/g-pilot-vs](https://m68gitlab.g-bits.com/aigc/g-pilot-vs)  
G-pilot jetbrains系列（IDEA、pycharm、Clion）扩展插件：[https://m68gitlab.g-bits.com/aigc/g-pilot-idea](https://m68gitlab.g-bits.com/aigc/g-pilot-idea)  
G-pilot VS、jetbrains系列（IDEA、pycharm、Clion）扩展插件后端：[https://m68gitlab.g-bits.com/aigc/g-pilot-vs-lsp](https://m68gitlab.g-bits.com/aigc/g-pilot-vs-lsp)

## 飞书使用交流群

目前G-pilot还在不断迭代和优化过程，欢迎大家点击[飞书使用交流群链接](https://applink.feishu.cn/client/chat/chatter/add_by_link?link_token=d44u4681-12a4-4ccc-8ae8-a8bd8d563695)加入飞书使用交流群给我们提供宝贵的修改和优化建议。大家在使用过程遇到任何问题都可以直接在群里沟通和交流，插件的新版本发布也会在群里进行通知。

## 功能

目前G-pilot支持单行代码补全功能、多行代码补全功能、注释生成代码功能，其他AI功能后续根据大家需求来开发。

## 安装方式

**（注意使用G-pilot插件前，需要将其他代码补全插件如Copilot、Comate、Marscode、TabNine等禁用，避免代码补全的功能发生冲突）**  
在IDEA菜单栏中选择**文件**，再点击**设置**打开设置界面，在左边栏中点击**插件**，然后选择**Marketplace**，在下方搜索：**G-pilot**，然后点击安装成功后，重启IDEA即可：  
![Image text](https://raw.githubusercontent.com/zhangwenhao66/g-pilot-vscode-img/refs/heads/main/img/idea%E5%AE%89%E8%A3%85%E7%95%8C%E9%9D%A2.jpg)  

## 使用方法

### 1. 单行代码补全功能

- **触发方式**

1. 代码文本发生改动的时候，会自动触发获取一次补全提示
2. 按组合键**shift+ctrl+空格**，可以主动触发获取一次单行代码补全提示

- **补全显示**

  补全成功触发后，会在光标后方以灰色字体将结果显示出来，如下图所示

  ![Image text](https://github.com/zhangwenhao66/g-pilot-vscode-img/blob/main/img/%E8%A1%A5%E5%85%A8%E6%98%BE%E7%A4%BA.jpg?raw=true)

- **补全采用**

按**tab**键即可采用全部补全

- **关闭补全显示**

  按**Esc**键即可关闭本次补全结果的显示

### 2. 多行代码补全功能

- **触发方式**

按组合键**shift+ctrl+enter**，可以主动触发获取一次多行代码补全提示
![Image text](https://github.com/zhangwenhao66/g-pilot-vscode-img/blob/main/img/%E5%A4%9A%E8%A1%8C%E8%A1%A5%E5%85%A8%E6%98%BE%E7%A4%BA.jpg?raw=true)

- **补全显示、补全采用、关闭补全显示**

  与单行代码补全功能一致

### 3. 注释生成代码功能

- **触发方式**

与多行代码补全同理，在注释行处或者注释行下一行通过组合键**shift+ctrl+enter**，也可以主动触发注释生成代码提示

- **补全显示、补全采用、关闭补全显示**

  与单行代码补全功能一致

### 4.重要参数设置

在IDEA菜单栏中选择**文件**，再点击**设置**打开设置界面，在左边栏点击展开**工具**选项，找到**G-pilot**，选择后则会显示出G-pilot相关的参数设置，如下图所示

![Image text](https://raw.githubusercontent.com/zhangwenhao66/g-pilot-vscode-img/refs/heads/main/img/idea%E8%AE%BE%E7%BD%AE%E7%95%8C%E9%9D%A2.jpg)

其中比较重要的参数如下：

- **代码补全推荐显示阈值**
  - 默认值：3
  - 范围：0~正无穷
  - 说明：数值越小则判断越严格，显示的补全代码推荐越少，显示的补全代码质量越高
  - 特殊值：0代表所有补全代码推荐都不进行显示。空代表正无穷，所有补全推荐都会显示出来

- **是否同意进行代码数据收集**
  - 默认：勾选
  - 说明：如果勾选了，则会在你每次采纳补全推荐的时候，将该数据发送给后端进行记录（发送的数据不包含用户信息，收集的是匿名数据）。这些数据将会保存用于后续G-pilot的模型的持续迭代升级。

- **采纳补全后是否自动跳转到行尾**
  - 默认：勾选
  - 说明：如果勾选了，则会在你每次采纳补全推荐后自动跳转到行尾。
