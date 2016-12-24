package articlestreamer.aggregator.service

/**
  * Handle the store containing the URLs temporarily stored
  */
trait URLStoreService {

  /**
    * Returns True if the provided URL already exists in the store
    */
  def exists(url: String): Boolean

  /**
    * Save a new URL in the store
    */
  def save(url: String): Unit

}
