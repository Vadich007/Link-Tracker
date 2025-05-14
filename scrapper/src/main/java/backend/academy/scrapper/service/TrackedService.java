package backend.academy.scrapper.service;

public interface TrackedService<T> {

    String getServicePrefix();

    boolean hasUpdate(String url);

    String getLastUpdateMessage(String url, T lastEvent);
}
