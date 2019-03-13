package lucene;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

class WordIterator implements InputIterator
{
    private Iterator<Word> wordIterator;
    private Word currentWord;

    WordIterator(Iterator<Word> wordIterator) {
        this.wordIterator = wordIterator;
    }

    public boolean hasContexts() {
        return true;
    }

    public boolean hasPayloads() {
        return true;
    }

    public Comparator<BytesRef> getComparator() {
        return null;
    }

    // This method needs to return the key for the record; this is the
    // text we'll be autocompleting against.
    public BytesRef next() {
        if (wordIterator.hasNext()) {
            currentWord = wordIterator.next();
            try {
                return new BytesRef(currentWord.word.getBytes("UTF8"));
            } catch (UnsupportedEncodingException e) {
                throw new Error("Couldn't convert to UTF-8");
            }
        } else {
            return null;
        }
    }

    // This method returns the payload for the record, which is
    // additional data that can be associated with a record and
    // returned when we do suggestion lookups.  In this example the
    // payload is a serialized Java object representing our product.
    public BytesRef payload() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(currentWord);
            out.close();
            return new BytesRef(bos.toByteArray());
        } catch (IOException e) {
            throw new Error("Well that's unfortunate.");
        }
    }

    // This method returns the contexts for the record, which we can
    // use to restrict suggestions.  In this example we use the
    // regions in which a product is sold.
    public Set<BytesRef> contexts() {
        try {
            Set<BytesRef> regions = new HashSet();
           // for (String region : currentWord.region) {
                regions.add(new BytesRef(currentWord.region.getBytes("UTF8")));
        //    }
            return regions;
        } catch (UnsupportedEncodingException e) {
            throw new Error("Couldn't convert to UTF-8");
        }
    }

    // This method helps us order our suggestions.  In this example we
    // use the number of products of this type that we've sold.
    public long weight() {
        return currentWord.rate;
    }
}