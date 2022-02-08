Steps to run the examples
1. right click project folder > configure > add gradle nature
2. include liblsl64.dll into your application's root directory, or a system folder
3. include jna-{version}.jar
4. right click src folder > build path > use as source folder
5. Run SendData to create a stream outlet and make the streaming data available for other platforms
6. Run ReceiveData to retrieve the streaming data