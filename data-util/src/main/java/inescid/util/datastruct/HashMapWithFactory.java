package inescid.util.datastruct;

import java.util.HashMap;

public class HashMapWithFactory<K, V> extends HashMap<K, V> {
	
	public interface ValueFactory {
		public Object newInstance(Object forKey);
	}
	
	ValueFactory valueFactory;
	
	public HashMapWithFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}
	public HashMapWithFactory(Class<V> valueClass) {
		this.valueFactory = new ValueFactory() {
			@Override
			public Object newInstance(Object forKey) {
				try {
					return valueClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		};
	}
	
	@Override
	public V get(Object key) {
		V val = super.get(key);
		if(val==null) {
			val=(V) valueFactory.newInstance(key);
			put((K)key, val);
		}
		return val;
	}
}
