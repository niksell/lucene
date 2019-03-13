package lucene;

public class Word  implements java.io.Serializable
{
    String word;
    String region;
    int rate;
    
    public Word(String word ,String region,int rate) {
        this.word = word;
        this.region=region;
        this.rate=rate;
       
    }
}
