<h1 align="center">XML DOM-Parser</h1>

<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#Requirements">Requirements</a>
    </li>
    <li>
        <a href = "#Getting-start">Getting-start</a>
    </li>
    <li>
        <a href="#contact">Contact</a>
    </li>
  </ol>
</details>

## Requirements

1) [jdk8](https://cmake.org/ "cmake home")
2) [javafx](https://openjfx.io/ "Javafx home page")
3) [Intellij idea](https://www.jetbrains.com/idea/download/ "Intellij idea download page")

## Getting start
<h4>DOM parser is a way to represent an xml document in a tree view enabling you to access any node in the 
document</h4>


#### In order to use this code for your purposes you have to implement and override the methods in `Paraser.java` Interface

<br/>

### 1) To read the xml file use this method

```java
    void parse(File xml_file) throws InterruptedException;
```

### 2) To write jdom or  w3 dom document to and xml file use this method

```java
    void write_xml_file(File file) throws TransformerException;
```
### 3) To create a new element and append it to dom tree use this method

```java
    void create_element(java.lang.Object object);
```

### 4) To encrypt an element you can use this method:

```java
    void encrypt_element(int selected_node, int leaf_index) throws JDOMException;
```

#### you can see `Paraser.java` inorder to see more method signatures;

## Contact

#### [Ali Ibrahim](ali.ibrahi.2@lit-co.net)<br/>
#### <i class="fa fa-google" aria-hidden="true"></i>[Gmail](allosheribraheem38@gmail.com)
#### <i class="fa fa-linkedin" aria-hidden="true"></i>[LinkedIn](https://www.linkedin.com/in/ali-ibrahim-b978b4181/)

    