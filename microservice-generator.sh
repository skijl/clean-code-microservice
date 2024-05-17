#!/bin/bash

# Check if the argument is provided
if [ ! -z "$1" ]; then
    cd "$1" || { echo "Error: Unable to navigate to $1"; exit 1; }
fi

# Check if src directory exists
if [ ! -d "src" ]; then
    echo "Error: 'src' directory not found in $1"
    exit 1
fi

# Find the directory containing the model directory
BASE_DIR=$(find src -type d -name "model" -printf "%h\n" | head -n 1)

# Check if model directory is found
if [ -z "$BASE_DIR" ]; then
    echo "Error: 'model' directory not found in 'src'"
    exit 1
fi

# Set the source directory for models
MODELS_DIR="$BASE_DIR/model"
STATIC_FILES_DIR="/d/scripts/static_files/main"

# For imports
base_package_name=$(echo "$BASE_DIR" | sed 's|.*java/||; s|/|.|g')

# Get type of Id
getIdType() {
    model_file=$(find "$MODELS_DIR" -maxdepth 1 -type f -name "*.java" -print -quit)
    if [ -n "$model_file" ]; then
        if [ ! -z "$1" ]; then
            model_file="$MODELS_DIR/$1.java"
            id_line=$(grep -n "@Id" "$model_file" | head -n 1 | cut -d ":" -f 1)
            private_line=$(awk "NR > $id_line && /private/ {print NR; exit}" "$model_file")
            id_type=$(awk "NR==$private_line" "$model_file" | awk '{print $2}')
            echo "$id_type"
        else
            id_line=$(grep -n "@Id" "$model_file" | head -n 1 | cut -d ":" -f 1)
            private_line=$(awk "NR > $id_line && /private/ {print NR; exit}" "$model_file")
            id_type=$(awk "NR==$private_line" "$model_file" | awk '{print $2}')
            echo "$id_type"
        fi
    else
        echo "No models are detected in /model dir"
        exit 1
    fi
}

# Function to generate DTO Requests---------------------------------------------------------------------------------------------------------
generate_reqest_dto() {
    TARGET_DIR="$BASE_DIR/dto/request"
    local model_file="$1"
    local request_type="DtoRequest"
    local model_name=$(basename "$model_file" .java)
    if [[ $model_name == *Model ]]; then
        model_name="${model_name%Model}"
    fi

    local lowercase_model_name=$(echo "$model_name" | tr '[:upper:]' '[:lower:]')
    local dto_dir="$TARGET_DIR"
    mkdir -p "$dto_dir"
    create_request_file="$dto_dir/${model_name}${request_type}.java"

    # Extract package name from the DTO directory structure
    package_name=$(dirname "${create_request_file}" | sed 's|.*java/||; s|/|.|g')

    # Add imports
    echo "package $package_name;" > "$create_request_file"
    echo "" >> "$create_request_file"
    echo "import jakarta.validation.constraints.NotBlank;" >> "$create_request_file"
    echo "import jakarta.validation.constraints.NotNull;" >> "$create_request_file"
    echo "import jakarta.validation.constraints.Positive;" >> "$create_request_file"
    echo "import lombok.Data;" >> "$create_request_file"
    echo "import lombok.AllArgsConstructor;" >> "$create_request_file"
    echo "import lombok.NoArgsConstructor;" >> "$create_request_file"
    echo "" >> "$create_request_file"

    if grep -q "BigDecimal" "$model_file"; then
        echo "import java.math.BigDecimal;" >> "$create_request_file"
    fi
    if grep -q " Date " "$model_file"; then
        echo "import java.util.Date;" >> "$create_request_file"
    fi
    echo "" >> "$create_request_file"

    # Generate CreateRequest class
    echo "@AllArgsConstructor" >> "$create_request_file"
    echo "@NoArgsConstructor" >> "$create_request_file"
    echo "@Data" >> "$create_request_file"
    echo "public class $(basename "$create_request_file" .java) {" >> "$create_request_file"

    # Extract fields from the original model class, excluding id field and LocalDateTime type
    fields=$(grep -E 'private .*;' "$model_file" | sed 's/private \([^ ]*\) \([^;]*\);/\1 \2/' | grep -v "id" | grep -v "LocalDateTime")

    # Iterate over fields
    while IFS= read -r field; do
        # Extract field type and name
        field_type=$(echo "$field" | awk '{print $1}')
        field_name=$(echo "$field" | awk '{print (substr($2,1,1)) substr($2,2)}')
        upper_field_name=$(echo "${field_name}" | sed -E 's/(^|[^A-Za-z])[a-z]/\U&/g' | sed -E 's/([A-Z])/ \1/g' | sed -E 's/^[[:space:]]+//')

        case $field_type in
        String|Long|Integer|BigDecimal|Double)
            ;;
        *)
            if [[ $field_name == *Model ]]; then
                field_name="${field_name%Model}"
            fi
            if [ -f "$MODELS_DIR/$field_type.java" ]; then
                local id_type=$(getIdType "$field_type")
                if [ -n "$id_type" ]; then
                    field_type="$id_type"
                    field_name="${field_name}Id"
                else
                    field_type="String"
                fi
            else
                field_type="String"
            fi
            ;;
        esac

        # Check field type and add validation annotations accordingly
        case "$field_type" in
            String)
                echo "    @NotNull(message = \"$upper_field_name cannot be null\")" >> "$create_request_file"
                echo "    @NotBlank(message = \"$upper_field_name cannot be blank\")" >> "$create_request_file"
                ;;
            Long|Integer|BigDecimal|Double)
                echo "    @Positive(message = \"$upper_field_name must be a positive number\")" >> "$create_request_file"
                echo "    @NotNull(message = \"$upper_field_name cannot be null\")" >> "$create_request_file"
                ;;
            *)
                # Leave other types without annotations
                ;;
        esac

        # Add field declaration to the class without indentation and with semicolon
        echo "    private ${field_type} ${field_name};" >> "$create_request_file"
        echo "" >> "$create_request_file"
    done <<< "$fields"

    # Close CreateRequest class
    echo "}" >> "$create_request_file"
}

# Function to generate DTO Responses---------------------------------------------------------------------------------------------------------
generate_response_dto() {
    TARGET_DIR="$BASE_DIR/dto/response"
    local model_file="$1"
    local response_type="DtoResponse"
    local model_name=$(basename "$model_file" .java)
    if [[ $model_name == *Model ]]; then
        model_name="${model_name%Model}"
    fi
    local lowercase_model_name=$(echo "$model_name" | tr '[:upper:]' '[:lower:]')
    local dto_dir="$TARGET_DIR"
    mkdir -p "$dto_dir"
    create_response_file="$dto_dir/${model_name}${response_type}.java"

    # Extract package name from the DTO directory structure
    package_name=$(dirname "${create_response_file}" | sed 's|.*java/||; s|/|.|g')

    # Add imports
    echo "package $package_name;" > "$create_response_file"
    echo "" >> "$create_response_file"

    echo "import lombok.Data;" >> "$create_response_file" 
    echo "import lombok.AllArgsConstructor;" >> "$create_response_file"
    echo "import lombok.NoArgsConstructor;" >> "$create_response_file"
    echo "" >> "$create_response_file"

    if grep -q "LocalDateTime" "$model_file"; then
        echo "import java.time.LocalDateTime;" >> "$create_response_file"
    fi
    if grep -q "BigDecimal" "$model_file"; then
        echo "import java.math.BigDecimal;" >> "$create_response_file"
    fi
    echo "" >> "$create_response_file"

    # Generate CreateResponse class
    echo "@AllArgsConstructor" >> "$create_response_file"
    echo "@NoArgsConstructor" >> "$create_response_file"
    echo "@Data" >> "$create_response_file"
    echo "public class $(basename "$create_response_file" .java) {" >> "$create_response_file"

    # Extract fields from the original model class, excluding id field and LocalDateTime type
    fields=$(grep -E 'private .*;' "$model_file" | sed 's/private \([^ ]*\) \([^;]*\);/\1 \2/')

    # Iterate over fields
    while IFS= read -r field; do
        # Extract field type and name
        field_type=$(echo "$field" | awk '{print $1}')
        field_name=$(echo "$field" | awk '{print $2}')

        case $field_type in
        String|Long|Integer|BigDecimal|Double|LocalDateTime)
            if [ $field_type == "LocalDateTime" ] ; then
            if ! sed -n '3p' "$create_response_file" | grep -q "import com.fasterxml.jackson.annotation.JsonFormat;"; then
                sed -i '3i\import com.fasterxml.jackson.annotation.JsonFormat;' "$create_response_file"
            fi
                echo "    @JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss\")" >> "$create_response_file"
            fi ;;
        *)
            if [[ $field_name == *Model ]]; then
                field_name="${field_name%Model}"
            fi
            if [ -f "$MODELS_DIR/$field_type.java" ]; then
                if [[ $field_type == *Model ]]; then
                    field_type="${field_type%Model}"
                fi
                field_type="${field_type}DtoResponse"
            fi
        esac

        # Add field declaration to the class without indentation and with semicolon
        echo "    private ${field_type} ${field_name};" >> "$create_response_file"
        echo "" >> "$create_response_file"
    done <<< "$fields"

    # Close CreateRequest class
    echo "}" >> "$create_response_file"
}
# Iterate over all Java files in the models directory
for model_file in "$MODELS_DIR"/*.java; do
    generate_reqest_dto "$model_file" "$request_type"
done
echo "DTO Requests generated successfully"


for model_file in "$MODELS_DIR"/*.java; do
    generate_response_dto "$model_file" "$response_type"
done
echo "DTO Responses generated successfully"

echo ""
echo "Now adjust your DTOs before running the next script"
read -p "Do you want to continue? (Y/n): " choice

# Check if the choice is 'y' or 'Y', then continue
if ! [[ "$choice" =~ ^[Yy]$ ]]; then
    exit 1
fi

# Function to generate DTO mappers---------------------------------------------------------------------------------------------------------
generate_dto_mapper() {
    local model_name="$1"
    local class_name="$1"
    if [ $# -eq 2 ]; then
        model_name="$2"  # Set model_name to second argument
    fi
    # Set the target directory for DTO mappers
    local lowercase_model_name=$(echo "$model_name" | tr '[:upper:]' '[:lower:]')
    local mapper_dir="$BASE_DIR/dto/dtoMapper"
    mkdir -p "$mapper_dir"
    create_mapper_file="$mapper_dir/${model_name}DtoMapper.java"

    # Extract package name from the DTO directory structure
    package_name=$(dirname "${create_mapper_file}" | sed 's|.*java/||; s|/|.|g')
    
    # Add imports for model and DTO classes
    echo "package $package_name;" > "$create_mapper_file"
    echo "" >> "$create_mapper_file"
    echo "import $base_package_name.model.$class_name;" >> "$create_mapper_file"
    echo "import ${base_package_name}.dto.request.${model_name}DtoRequest;" >> "$create_mapper_file"
    echo "import ${base_package_name}.dto.response.${model_name}DtoResponse;" >> "$create_mapper_file"
    echo "" >> "$create_mapper_file"

    echo "public class ${model_name}DtoMapper {" >> "$create_mapper_file"
    echo "" >> "$create_mapper_file"

    # Generate DTO Mapper class
    # Generate toModel method
    echo "    public static $class_name toModel(${model_name}DtoRequest request) {" >> "$create_mapper_file"
    echo "        $class_name model = new $class_name();" >> "$create_mapper_file"
    echo "" >> "$create_mapper_file"
    # Iterate over fields in the model
    grep -E 'private .*;' "$model_file" | sed 's/private \([^ ]*\) \([^;]*\);/\1 \2/' | while read -r field; do
        field_type=$(echo "$field" | awk '{print $1}')
        field_name=$(echo "$field" | awk '{print $2}')
        full_field_name="$(echo "${field_name:0:1}" | tr '[:lower:]' '[:upper:]')${field_name:1}"
        if [[ $field_name == *Model ]]; then
            field_name="${field_name%Model}"
        fi
        # Check if field exists in CreateRequest and map it
        if grep -q "private .* $field_name;" "$BASE_DIR/dto/request/${model_name}DtoRequest.java"; then
            echo "        model.set${field_name^}(request.get${field_name^}());" >> "$create_mapper_file"
        elif grep -q "private .* ${field_name}Id;" "$BASE_DIR/dto/request/${model_name}DtoRequest.java"; then
            lower_field_type="$(echo "${field_name:0:1}" | tr '[:upper:]' '[:lower:]')${field_name:1}"
            field_name="$(echo "${field_name:0:1}" | tr '[:lower:]' '[:upper:]')${field_name:1}"
            echo "        ${field_type} ${lower_field_type} = new ${field_type}();" >> "$create_mapper_file"
            echo "        ${lower_field_type}.setId(request.get${field_name}Id());" >> "$create_mapper_file"
            echo "        model.set${full_field_name}(${lower_field_type});" >> "$create_mapper_file"
            sed -i "3i\import $base_package_name.model.${field_type};" "$create_mapper_file"
        fi
    done
    echo "" >> "$create_mapper_file"
    echo "        return model;" >> "$create_mapper_file"
    echo "    }" >> "$create_mapper_file"
    echo "" >> "$create_mapper_file"

    # Generate toResponse method
    echo "    public static ${model_name}DtoResponse toResponse(${class_name} model) {" >> "$create_mapper_file"
    echo "        ${model_name}DtoResponse response = new ${model_name}DtoResponse();" >> "$create_mapper_file"
    echo "" >> "$create_mapper_file"
    # Iterate over fields in the model
    grep -E 'private .*;' "$model_file" | sed 's/private \([^ ]*\) \([^;]*\);/\1 \2/' | while read -r field; do
        field_type=$(echo "$field" | awk '{print $1}')
        field_name=$(echo "$field" | awk '{print $2}')
        # Check if field exists in CreateResponse and map it
        if grep -q "private $field_type $field_name;" "$BASE_DIR/dto/response/${model_name}DtoResponse.java"; then
            echo "        response.set${field_name^}(model.get${field_name^}());" >> "$create_mapper_file"
        else 
            field_type=$(echo "$field" | awk '{print $1}')
            if [[ $field_type == *Model ]]; then
                field_type="${field_type%Model}"
            fi
            field_name="$(echo "${field_name:0:1}" | tr '[:lower:]' '[:upper:]')${field_name:1}"
            response_field_name=$(grep "private ${field_type}DtoResponse .*;" "$BASE_DIR/dto/response/${model_name}DtoResponse.java" | sed -E 's/private '${field_type}'DtoResponse (.+?);/\1/' | sed -E 's/.{3}//' | sed 's/./\U&/' | sed 's/^.//')
            response_field_name="$(echo "${response_field_name:0:1}" | tr '[:lower:]' '[:upper:]')${response_field_name:1}"
            if grep -q "private ${field_type}DtoResponse .*;" "$BASE_DIR/dto/response/${model_name}DtoResponse.java"; then
                echo "        response.set${response_field_name}(${field_type}DtoMapper.toResponse(model.get${field_name}()));" >> "$create_mapper_file"
            fi
        fi
    done
    echo "" >> "$create_mapper_file"
    echo "        return response;" >> "$create_mapper_file"
    echo "    }" >> "$create_mapper_file"
    echo "}" >> "$create_mapper_file"
}

for model_file in "$MODELS_DIR"/*.java; do
    model_name=$(basename "$model_file" .java)
    if [[ $model_name == *Model ]]; then
        model_name_without_suffix="${model_name%Model}"
        generate_dto_mapper "$model_name" "$model_name_without_suffix"
    else
        generate_dto_mapper "$model_name"
    fi
done

echo "DTO Mappers generated successfully"

# Function to generate repository interface---------------------------------------------------------------------------------------------------------
REPOSITORY_DIR="$BASE_DIR/repository"
mkdir -p "$REPOSITORY_DIR"

generate_repository() {
    local model_name="$1"
    local class_name="$1"
    if [ $# -eq 2 ]; then
        model_name="$2"  # Set model_name to second argument
    fi

    local repository_file="$REPOSITORY_DIR/${model_name}Repository.java"
    package_name=$(dirname "${repository_file}" | sed 's|.*java/||; s|/|.|g')

    # Add imports for model and DTO classes
    echo "package $package_name;" > "$repository_file"
    echo "" >> "$repository_file"
    echo "import $base_package_name.model.$class_name;" >> "$repository_file"

    # Get the type of id
    id_line=$(grep -n "@Id" "$model_file" | head -n 1 | cut -d ":" -f 1)
    private_line=$(awk "NR > $id_line && /private/ {print NR; exit}" "$model_file")
    id_type=$(awk "NR==$private_line" "$model_file" | awk '{print $2}')

    # Check if the model class has the @Entity annotation
    if grep -q "@Entity" "$model_file"; then
        repository_extension="JpaRepository<${class_name}, ${id_type}>"
        echo "import org.springframework.data.jpa.repository.JpaRepository;" >> "$repository_file"
    # Check if the model class has the @Document annotation
    elif grep -q "@Document" "$model_file"; then
        repository_extension="MongoRepository<${class_name}, ${id_type}>"
        echo "import org.springframework.data.mongodb.repository.MongoRepository;" >> "$repository_file"
    else
        echo "Error: Model class '$model_name' does not have @Entity or @Document annotation"
        exit 1
    fi

    # Generate repository interface
    echo "" >> "$repository_file"
    echo "public interface ${model_name}Repository extends $repository_extension {" >> "$repository_file"
    echo "" >> "$repository_file"
    echo "}" >> "$repository_file"
}

# Iterate over all Java files in the models directory
for model_file in "$MODELS_DIR"/*.java; do
    model_name=$(basename "$model_file" .java)
    if [[ $model_name == *Model ]]; then
        model_name_without_suffix="${model_name%Model}"
        generate_repository "$model_name" "$model_name_without_suffix"
    else
        generate_repository "$model_name"
    fi
done

echo "Repository interfaces generated successfully."

# Function to generate service interface---------------------------------------------------------------------------------------------------------
generate_crud_service_interface() {
    id_type=$1
    local service_file="$BASE_DIR/service/CrudService.java"
    package_name=$(dirname "${service_file}" | sed 's|.*java/||; s|/|.|g')

    # Add package and imports for model class
    echo "package $package_name;" > "$service_file"
    echo "" >> "$service_file"
    echo "import java.util.List;" >> "$service_file"
    echo "" >> "$service_file"
    # Generate service interface
    echo "public interface CrudService<T> {" >> "$service_file"
    echo "    public T create(T model);" >> "$service_file"
    echo "    public T getById($id_type id);" >> "$service_file"
    echo "    public List<T> getAll();" >> "$service_file"
    echo "    public T update($id_type id, T model);" >> "$service_file"
    echo "    public Boolean deleteById($id_type id);" >> "$service_file"
    echo "}" >> "$service_file"
}

mkdir -p "$BASE_DIR/service"
{
# Find the first Java file in the models directory
model_file=$(find "$MODELS_DIR" -maxdepth 1 -type f -name "*.java" -print -quit)
if [ -n "$model_file" ]; then
    # Get the type of id
    id_line=$(grep -n "@Id" "$model_file" | head -n 1 | cut -d ":" -f 1)
    private_line=$(awk "NR > $id_line && /private/ {print NR; exit}" "$model_file")
    id_type=$(awk "NR==$private_line" "$model_file" | awk '{print $2}')
    generate_crud_service_interface $id_type
fi
}

# Function to generate service interface---------------------------------------------------------------------------------------------------------
generate_service_interface() {
    local model_name="$1"
    local class_name="$1"
    if [ $# -eq 2 ]; then
        model_name="$2"  # Set model_name to second argument
    fi

    local lowercase_model_name="${model_name,}"
    local service_file="$BASE_DIR/service/${model_name}Service.java"
    package_name=$(dirname "${service_file}" | sed 's|.*java/||; s|/|.|g')

    # Add package and imports for model class
    echo "package $package_name;" > "$service_file"
    echo "" >> "$service_file"
    echo "import $base_package_name.model.$class_name;" >> "$service_file"
    echo "" >> "$service_file"

    # Get the type of id
    id_line=$(grep -n "@Id" "$model_file" | head -n 1 | cut -d ":" -f 1)
    private_line=$(awk "NR > $id_line && /private/ {print NR; exit}" "$model_file")
    id_type=$(awk "NR==$private_line" "$model_file" | awk '{print $2}')

    # Generate service interface
    echo "public interface ${model_name}Service extends CrudService<${class_name}>{" >> "$service_file"
    echo "" >> "$service_file"
    echo "}" >> "$service_file"
}

mkdir -p "$BASE_DIR/service"

# Iterate over all Java files in the models directory
for model_file in "$MODELS_DIR"/*.java; do
    model_name=$(basename "$model_file" .java)
    if [[ $model_name == *Model ]]; then
        model_name_without_suffix="${model_name%Model}"
        generate_service_interface "$model_name" "$model_name_without_suffix"
    else
        generate_service_interface "$model_name"
    fi
done
echo "Service interfaces generated successfully."
# Function to generate exception package and exceptions---------------------------------------------------------------------------------------------------------
generate_exceptions_package(){
    local EXCEPTION_DIR="$BASE_DIR/exception"
    mkdir -p "$EXCEPTION_DIR"
    package_name=$(realpath "${EXCEPTION_DIR}" | sed 's|.*java/||; s|/|.|g')
    
    # Generate ExceptionPayload class
    echo "package $package_name;" > "$EXCEPTION_DIR/ExceptionPayload.java"
    echo "" >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo 'import lombok.AllArgsConstructor;' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo 'import lombok.Data;' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo 'import lombok.NoArgsConstructor;' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo '' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo '@AllArgsConstructor' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo '@NoArgsConstructor' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo '@Data' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo 'public class ExceptionPayload {' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo '    private Object errorMessage;' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo '    private String documentationUri;' >> "$EXCEPTION_DIR/ExceptionPayload.java"
    echo '}' >> "$EXCEPTION_DIR/ExceptionPayload.java"

    # Generate EntityNotFoundException class
    echo "package $package_name;" > "$EXCEPTION_DIR/EntityNotFoundException.java"
    echo "" >> "$EXCEPTION_DIR/EntityNotFoundException.java"
    echo 'public class EntityNotFoundException extends RuntimeException{' >> "$EXCEPTION_DIR/EntityNotFoundException.java"
    echo '    public EntityNotFoundException(String message) {' >> "$EXCEPTION_DIR/EntityNotFoundException.java"
    echo '        super(message);' >> "$EXCEPTION_DIR/EntityNotFoundException.java"
    echo '    }' >> "$EXCEPTION_DIR/EntityNotFoundException.java"
    echo '}' >> "$EXCEPTION_DIR/EntityNotFoundException.java"

    # Generate GlobalExceptionHandler class
    echo "package $package_name;" > "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo "" >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.beans.factory.annotation.Value;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.dao.DataAccessException;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.http.HttpStatus;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.http.ResponseEntity;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.validation.FieldError;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.web.bind.MethodArgumentNotValidException;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.web.bind.annotation.ExceptionHandler;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import org.springframework.web.bind.annotation.RestControllerAdvice;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import java.util.HashMap;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'import java.util.Map;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '@RestControllerAdvice' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo 'public class GlobalExceptionHandler {' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    private final String DOCUMENTATION_URI;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    public GlobalExceptionHandler(@Value("${swagger.documentation.uri}") String DOCUMENTATION_URI) {' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '        this.DOCUMENTATION_URI = DOCUMENTATION_URI;' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    }' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    @ExceptionHandler(MethodArgumentNotValidException.class)' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    public ResponseEntity<Object> handleConstraintViolationException(MethodArgumentNotValidException e) {' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '        Map<String, String> errors = new HashMap<>();' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '        e.getBindingResult().getAllErrors().forEach(error -> {' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '            String fieldName = ((FieldError) error).getField();' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '            String errorMessage = error.getDefaultMessage();' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '            errors.put(fieldName, errorMessage);' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '        });' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '        return new ResponseEntity<>(new ExceptionPayload(errors, DOCUMENTATION_URI), HttpStatus.BAD_REQUEST);' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    }' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    @ExceptionHandler(EntityNotFoundException.class)' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    public ResponseEntity<Object> handlerEntityNotFoundException(EntityNotFoundException e){' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '        return new ResponseEntity<>(new ExceptionPayload(e.getMessage(), DOCUMENTATION_URI), HttpStatus.NOT_FOUND);' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    }' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    @ExceptionHandler(DataAccessException.class)' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    public ResponseEntity<Object> handlerDataAccessException(DataAccessException e){' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '        return new ResponseEntity<>(new ExceptionPayload(e.getMessage(), DOCUMENTATION_URI), HttpStatus.SERVICE_UNAVAILABLE);' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '    }' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
    echo '}' >> "$EXCEPTION_DIR/GlobalExceptionHandler.java"
}
generate_exceptions_package
echo "Exception implementations generated successfully."
# Function to generate service implementation class---------------------------------------------------------------------------------------------------------
generate_service_impl_class() {
    local SERVICE_IMPL_DIR="$BASE_DIR/service/impl"
    local model_name="$1"
    local class_name="$1"
    if [ $# -eq 2 ]; then
        model_name="$2"  # Set model_name to second argument
    fi
    
    local lowercase_model_name="${model_name,}"
    local service_impl_file="$SERVICE_IMPL_DIR/${model_name}ServiceImpl.java"
    package_name=$(dirname "${service_impl_file}" | sed 's|.*java/||; s|/|.|g')

    # Get the type of id
    id_line=$(grep -n "@Id" "$model_file" | head -n 1 | cut -d ":" -f 1)
    private_line=$(awk "NR > $id_line && /private/ {print NR; exit}" "$model_file")
    id_type=$(awk "NR==$private_line" "$model_file" | awk '{print $2}')

    # Add imports for model class and service interface
    echo "package $package_name;" > "$service_impl_file"
    echo "" >> "$service_impl_file"
    echo "import $base_package_name.exception.EntityNotFoundException;" >> "$service_impl_file"
    echo "import $base_package_name.model.$class_name;" >> "$service_impl_file"
    echo "import $base_package_name.repository.${model_name}Repository;" >> "$service_impl_file"
    echo "import $base_package_name.service.${model_name}Service;" >> "$service_impl_file"
    echo "import lombok.extern.slf4j.Slf4j;" >> "$service_impl_file"
    echo "import org.springframework.stereotype.Service;" >> "$service_impl_file"
    echo "" >> "$service_impl_file"
    echo "import java.util.List;" >> "$service_impl_file"
    echo "" >> "$service_impl_file"

    # Generate service implementation class
    echo "@Slf4j" >> "$service_impl_file"
    echo "@Service" >> "$service_impl_file"
    echo "public class ${model_name}ServiceImpl implements ${model_name}Service {" >> "$service_impl_file"
    echo "    private final ${model_name}Repository ${lowercase_model_name}Repository;" >> "$service_impl_file"
    echo "" >> "$service_impl_file"
    echo "    public ${model_name}ServiceImpl(${model_name}Repository ${lowercase_model_name}Repository) {" >> "$service_impl_file"
    echo "        this.${lowercase_model_name}Repository = ${lowercase_model_name}Repository;" >> "$service_impl_file"
    echo "    }" >> "$service_impl_file"
    echo "" >> "$service_impl_file"

    echo "    @Override" >> "$service_impl_file"
    echo "    public $class_name create($class_name $lowercase_model_name) {" >> "$service_impl_file"
    echo "        log.info(\"$class_name create: {}\", $lowercase_model_name);" >> "$service_impl_file"


    # Pattern to match
    pattern="model.set"

    # Flag to track the conditions
    preprevious_line_is_new=false
    previous_line_is_new=false
    previous_line_is_not_empty=false

    foreign_services=
    service=
    # Read the input file line by line
    while IFS= read -r line; do
        # Check if the current line matches the pattern
        if [[ $line == *"$pattern"* ]]; then
            # Extract the object before "(" sign
            object=$(echo "$line" | awk -F"$pattern" '{print $2}' | awk -F'(' '{print $1}')
            # Check the conditions for appending to the output file
            if $previous_line_is_not_empty && $preprevious_line_is_new; then
                service_name="$(echo "${service:0:1}" | tr '[:upper:]' '[:lower:]')${service:1}"
                if [ -n "$foreign_services" ]; then
                    foreign_services="${foreign_services}\n"
                fi
                foreign_services=${foreign_services}"        ${lowercase_model_name}.set$object(${service_name}.getById(${lowercase_model_name}.get$object().getId()));"
                # Temporary file to store modified content
                temp_file=$(mktemp)
                # Flag to track whether we've encountered 'public' keyword
                public_encountered=false

                # Flag to track whether we're inside the class definition
                inside_class=false
                # Read the file line by line
                while IFS= read -r line; do
                    change_line=false
                    # Check if we've encountered the class definition
                    if [[ $line == *"public class"* ]]; then
                        inside_class=true
                    fi
                    if [[ $line == "import lombok.extern.slf4j.Slf4j;" ]]; then
                        echo "import $base_package_name.service.$service;" >> "$temp_file"
                    fi
                    
                    # If we're inside the class definition
                    if $inside_class; then
                        # If we encounter an empty line
                        if [[ -z "${line// }" ]]; then
                            # Add new line
                            echo "    private final $service $service_name;" >> "$temp_file"
                        fi
                        
                        # If we encounter 'public' keyword
                        if [[ $line == *"public "* ]]; then
                            # Set the flag to true
                            public_encountered=true
                        fi
                        
                        # If we've encountered 'public' and '('
                        if $public_encountered && [[ $line == *"("* ]]; then
                            before=$(echo "$line" | awk -F'(' '{print $1}')
                            after=$(echo "$line" | awk -F'(' '{print $2}')

                            # Add the line with '$service $service_name,'
                            echo "$before($service $service_name, $after" >> "$temp_file"
                            echo "        this.$service_name = $service_name;" >> "$temp_file"
                            # Reset the flags
                            public_encountered=false
                            inside_class=false
                            change_line=true
                        fi
                    fi
                    
                    # Write the original line to the temporary file
                    if ! $change_line; then
                        echo "$line" >> "$temp_file"
                    fi
                    
                done < "$service_impl_file"

                # Move the temporary file to the original file
                mv "$temp_file" "$service_impl_file"

            fi
        fi
        
        # Update the flags for the next iteration
        preprevious_line_is_new=$previous_line_is_new
        previous_line_is_not_empty=true
        
        # Check if the current line is not empty
        if [[ -z "${line// }" ]]; then
            previous_line_is_not_empty=false
        fi
        
        # Check if the current line contains '= new'
        if [[ $line == *"= new"* ]]; then
            service=$(echo "$line" | awk -F'        ' '{print $2}' | awk -F' ' '{print $1}')
            if [[ $service == *Model ]]; then
                service="${service%Model}"
            fi
            service="${service}Service"
            previous_line_is_new=true
        else
            previous_line_is_new=false
        fi
    done < "$BASE_DIR/dto/dtoMapper/${model_name}DtoMapper.java"
    echo -e "$foreign_services" >> "$service_impl_file"
    echo "        return ${lowercase_model_name}Repository.save($lowercase_model_name);" >> "$service_impl_file"
    echo "    }" >> "$service_impl_file"
    echo "" >> "$service_impl_file"

    echo "    @Override" >> "$service_impl_file"
    echo "    public $class_name getById($id_type id) {" >> "$service_impl_file"
    echo "        log.info(\"$class_name get by id: {}\", id);" >> "$service_impl_file"
    echo "        return ${lowercase_model_name}Repository.findById(id).orElseThrow(()->new EntityNotFoundException(\"${model_name} with id: \" + id + \" does not exist\"));" >> "$service_impl_file"
    echo "    }" >> "$service_impl_file"
    echo "" >> "$service_impl_file"

    echo "    @Override" >> "$service_impl_file"
    echo "    public List<$class_name> getAll() {" >> "$service_impl_file"
    echo "        log.info(\"$class_name get all\");" >> "$service_impl_file"
    echo "        return ${lowercase_model_name}Repository.findAll();" >> "$service_impl_file"
    echo "    }" >> "$service_impl_file"
    echo "" >> "$service_impl_file"

    echo "    @Override" >> "$service_impl_file"
    echo "    public $class_name update($id_type id, $class_name $lowercase_model_name) {" >> "$service_impl_file"
    echo "        getById(id);" >> "$service_impl_file"
    echo "        $lowercase_model_name.setId(id);" >> "$service_impl_file"
    echo -e "$foreign_services" >> "$service_impl_file"
    # # Map the fields from model to updatedModel
    # grep -E 'private .*;' "$model_file" | sed 's/private \([^ ]*\) \([^;]*\);/\1 \2/' | while read -r field; do
    #     field_name=$(echo "$field" | awk '{print $2}')
    #     # Check if field exists in CreateRequest and map it
    #     if grep -q "private .* $field_name;" "$BASE_DIR/dto/request/${model_name}DtoRequest.java"; then
    #         echo "        updated$model_name.set${field_name^}($lowercase_model_name.get${field_name^}());" >> "$service_impl_file"
    #     fi
    # done
    echo "        log.info(\"$class_name update by id: {}\", $lowercase_model_name);" >> "$service_impl_file"
    echo "        return ${lowercase_model_name}Repository.save($lowercase_model_name);" >> "$service_impl_file"
    echo "    }" >> "$service_impl_file"
    echo "" >> "$service_impl_file"

    echo "    @Override" >> "$service_impl_file"
    echo "    public Boolean deleteById($id_type id) {" >> "$service_impl_file"
    echo "        log.info(\"$class_name delete by id: {}\", id);" >> "$service_impl_file"   
    echo "        ${lowercase_model_name}Repository.deleteById(id);" >> "$service_impl_file"
    echo "        return true;" >> "$service_impl_file"
    echo "    }" >> "$service_impl_file"
    echo "}" >> "$service_impl_file"
}

mkdir -p "$BASE_DIR"/service/impl

# Iterate over all Java files in the models directory
for model_file in "$MODELS_DIR"/*.java; do
    model_name=$(basename "$model_file" .java)
    if [[ $model_name == *Model ]]; then
        model_name_without_suffix="${model_name%Model}"
        generate_service_impl_class "$model_name" "$model_name_without_suffix"
    else
        generate_service_impl_class "$model_name"
    fi
done

echo "Service implementations generated successfully"

# Function to generate controller class---------------------------------------------------------------------------------------------------------
generate_controller() {
    local model_name="$1"
    local class_name="$1"
    if [ $# -eq 2 ]; then
        model_name="$2"  # Set model_name to second argument
    fi
    local lowercase_model_name=$(echo "${model_name:0:1}" | tr '[:upper:]' '[:lower:]')${model_name:1}
    local controller_file="$CONTROLLER_DIR/${model_name}Controller.java"
    local lowercase_controller_name="${model_name}Controller"
    local request_model_name="$(echo "$lowercase_model_name" | sed 's/\([A-Z]\)/-\1/g' | tr '[:upper:]' '[:lower:]')"
    package_name=$(dirname "${controller_file}" | sed 's|.*java/||; s|/|.|g')

    # Get the type of id
    id_line=$(grep -n "@Id" "$model_file" | head -n 1 | cut -d ":" -f 1)
    private_line=$(awk "NR > $id_line && /private/ {print NR; exit}" "$model_file")
    id_type=$(awk "NR==$private_line" "$model_file" | awk '{print $2}')

    # Add imports for model class and service interface
    echo "package ${package_name};" > "$controller_file"
    echo "" >> "$controller_file"

    echo "import $base_package_name.dto.dtoMapper.${model_name}DtoMapper;" >> "$controller_file"
    echo "import $base_package_name.dto.request.${model_name}DtoRequest;" >> "$controller_file"
    echo "import $base_package_name.dto.response.${model_name}DtoResponse;" >> "$controller_file"
    echo "import $base_package_name.model.${class_name};" >> "$controller_file"
    echo "import $base_package_name.service.${model_name}Service;" >> "$controller_file"
    sed "s~\${model_name}~$model_name~g; s~\${lowercase_model_name}~$lowercase_model_name~g; s~\${request_model_name}~$request_model_name~g; s~\${id_type}~$id_type~g; s~\${class_name}~$class_name~g" "$STATIC_FILES_DIR/controller/static1" >> "$controller_file"
}

# Create controller directory if it doesn't exist
CONTROLLER_DIR="$BASE_DIR/controller"
mkdir -p "$CONTROLLER_DIR"

# Iterate over all Java files in the models directory
for model_file in "$MODELS_DIR"/*.java; do
    model_name=$(basename "$model_file" .java)
    if [[ $model_name == *Model ]]; then
        model_name_without_suffix="${model_name%Model}"
        generate_controller "$model_name" "$model_name_without_suffix"
    else
        generate_controller "$model_name"
    fi
done
echo "Controllers generated successfully"