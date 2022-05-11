# Vector Space Information Retrieval Model Implementation
 
This program implements the Vector Space Information Retrieval model for searching relevant documents,
demonstrated on the Cranfield corpus.
<br/>
<br/>
The program will read through the the cran.all.1400 corpus and ask the user for a query ID from the cran.qry file.
The program will then calculate the Cosine Similarity Scores for each document in relation to the query to then
create a list of top k results for relevant documents, where k is the number of results that the user wishes to
preview.
<br/>
<br/>
**How to Compile:**
<br/>
*javac -O -cp ".\opennlp-tools-1.9.1.jar" .\VectorSpaceModelIR.java*
<br/>
<br/>
**How to Run and their Parameters:**
<br/>
*java -cp ".\opennlp-tools-1.9.1.jar" VectorSpaceModelIR .\cranfield-corpus\cran.all.1400 .\cranfield-corpus\cran.qry*
