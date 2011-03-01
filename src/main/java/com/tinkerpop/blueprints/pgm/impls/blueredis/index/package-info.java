/**
 * <p>
 *     Basic indexing support supplied with Blueredis is based on
 *     <a href="http://playnice.ly/blog/2010/05/05/a-fast-fuzzy-full-text-index-using-redis/">A fast, fuzzy, full-text index using Redis</a>
 * </p>
 * <p>
 *     <strong>WARNING! Bundled indexing implemetation is quite resource intensive, creates lots of key-value pairs in redis
 *     and negatively affects performance. Use it at your own risk. See below for description</strong>
 * </p>
 * <p>
 *     Basically, the algorithm is as follows:
 *     <ul>
 *         <li> take incoming key, value and vertex</li>
 *         <li> calculate Metaphone for incoming value</li>
 *         <li> add vertex id to key:metaphone_of_value</li>
 *     </ul>
 * </p>
 * <p>
 *     E.g. you have a vertex with id=123. It has a property "name" equal to "John Smith". It means:
 *     <ul>
 *         <li> key="name", value="John Smith"</li>
 *         <li> metaphone for John Smith is JNSM0</li>
 *         <li> 123 will be added to name:JNSM0</li>
 *     </ul>
 * </p>
 * <p>
 *     Of course, reality is slightly more complicated than that:
 *     <ul>
 *         <li>there are automatic and manual indices</li>
 *         <li>indices are accessed by their names, there can be several automatic and several manual indices on a
 *             database</li>
 *         <li>an index may index vertices or edges, but not both</li>
 *         <li>indices may index all keys or only a subset of keys</li>
 *         <li>indices must persist and be accessible by their name and for the same set of keys as before after a
 *             database restart</li>
 *     </ul>
 *     So, we have to:
 *     <ul>
 *         <li>create a separate index entry for each auto/manual... with separate subentries for vertices and edges</li>
 *         <li>
 *             <ul>
 *                 <li>for each index key in an index save that key
 *                     <ul>
 *                         <li>for each key save vertex/edge ids the key points to</li>
 *                     </ul>
 *                 </li>
 *                 <li>for each index entry save list of keys that it holds</li>
 *             </ul>
 *         </li>
 *         <li>save a list of all indices</li>
 *     </ul>
 *     Sounds like a lot of work and, well, it is. Since redis is a key-value only store, there's a lot of extra work
 *     that you have to do to make all this work.
 *     <ul>
 *         <li>Indices are stored in keys that all start with an <code><strong>index:<em>...</em></strong></code></li>
 *         <li><code><strong>index:auto</strong></code> - prefix for all automatic indices</li>
 *         <li><code><strong>index:manual</strong></code> - prefix for all manual indices</li>
 *         <ul>
 *             For all of the above:
 *             <li><code><strong>index:<em>type</em>:<em>index_name</em></strong></code>, where <code><em>index_name</em></code>
 *                 is a user-supplied index name - prefix for index data for <em>index_name</em></code>
 *             </li>
 *             <li><code><strong>index:<em>type</em>:<em>index_name</em>:<em>key_name</em></strong></code>,
 *                 where <code><em>key_name</em></code>
 *                 is a user-supplied key name - prefix for index data for <em>key_name</em> key
 *             </li>
 *             <li><code><strong>index:<em>type</em>:<em>index_name</em>:<em>key_name</em>:<em>metaphone</em></strong></code>,
 *                 where <code><em>metaphone</em></code>
 *                 is a calculated metaphone for a value - contains a list of vertex ids that have a key <em>key_name</em>
 *                 with a value whose metaphone matches <em>metaphone</em>
 *             </li>
 *         </ul>
 *         <li><code><strong>index:meta:indices:auto</strong></code> - a list of automatic indices</li>
 *         <li><code><strong>index:meta:indices:manual</strong></code> - a list of manual indices</li>
 *         <li><code><strong>index:meta:auto</strong></code> - prefix for all meta information on automatic indices</li>
 *         <li><code><strong>index:meta:manual</strong></code> - prefix for all meta information on manual indices</li>
 *         <ul>
 *             For index:meta:(auto|manual) above:
 *             <li><code><strong>index:meta:<em>type</em>:<em>index_name</em>:class</strong></code>, where <code><em>index_name</em></code>
 *                 is a user-supplied index name - contains what class the index works on: vertex, edge, etc.
 *             </li>
 *             <li><code><strong>index:meta:<em>type</em>:<em>index_name</em>:keys</strong></code>, where <code><em>index_name</em></code>
 *                 is a user-supplied index name - contains a list of keys for this index
 *             </li>
 *         </ul>
 *     </ul>
 *     So, as you see, indexing support manipulates quite a few values, so if speed is your primary concern, do not tur on
 *     default indexing
 * </p>
 *
 * @since 0.2
 * @since 0.3
 * @see <a href="http://playnice.ly/blog/2010/05/05/a-fast-fuzzy-full-text-index-using-redis">A fast, fuzzy, full-text index using Redis</a>
 */
package com.tinkerpop.blueprints.pgm.impls.blueredis.index;